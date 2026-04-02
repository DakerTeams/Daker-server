package com.daker.domain.team.service;

import com.daker.domain.hackathon.domain.Hackathon;
import com.daker.domain.hackathon.domain.HackathonRegistration;
import com.daker.domain.hackathon.repository.HackathonRegistrationRepository;
import com.daker.domain.hackathon.repository.HackathonRepository;
import com.daker.domain.team.domain.*;
import com.daker.domain.team.dto.*;
import com.daker.domain.team.repository.*;
import com.daker.domain.user.domain.User;
import com.daker.domain.user.repository.UserRepository;
import com.daker.global.exception.CustomException;
import com.daker.global.exception.ErrorCode;
import com.daker.global.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final TeamApplicationRepository teamApplicationRepository;
    private final HackathonRepository hackathonRepository;
    private final HackathonRegistrationRepository registrationRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public PageResponse<TeamSummaryResponse> getTeams(Long hackathonId, Boolean isOpen, String q, Pageable pageable) {
        return new PageResponse<>(
                teamRepository.findAllWithFilters(hackathonId, isOpen, q, pageable)
                        .map(TeamSummaryResponse::new)
        );
    }

    @Transactional(readOnly = true)
    public List<TeamSummaryResponse> getMyTeams(Long userId) {
        return teamRepository.findAllByUserId(userId).stream()
                .map(TeamSummaryResponse::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public TeamDetailResponse getTeam(Long teamId) {
        Team team = teamRepository.findByIdWithDetails(teamId)
                .orElseThrow(() -> new CustomException(ErrorCode.TEAM_NOT_FOUND));
        return new TeamDetailResponse(team);
    }

    @Transactional
    public TeamDetailResponse createTeam(TeamCreateRequest request, Long userId) {
        Hackathon hackathon = null;

        if (request.getHackathonId() != null) {
            hackathon = hackathonRepository.findByIdAndDeletedFalse(request.getHackathonId())
                    .orElseThrow(() -> new CustomException(ErrorCode.HACKATHON_NOT_FOUND));

            if (!hackathon.isRegistrationOpen()) {
                throw new CustomException(ErrorCode.REGISTRATION_PERIOD_INVALID);
            }

            // 해당 해커톤에 이미 팀이 있는지 확인
            if (teamMemberRepository.existsByUserIdAndHackathonId(userId, hackathon.getId())) {
                throw new CustomException(ErrorCode.TEAM_ALREADY_EXISTS);
            }
        }

        User leader = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        int defaultMaxMemberCount = hackathon != null ? hackathon.getMaxTeamSize() : 5;

        Team team = Team.builder()
                .hackathon(hackathon)
                .leader(leader)
                .name(request.getName())
                .description(request.getDescription())
                .isOpen(request.getIsOpen() != null ? request.getIsOpen() : true)
                .isPublic(request.getIsPublic() != null ? request.getIsPublic() : true)
                .maxMemberCount(request.getMaxMemberCount() != null ? request.getMaxMemberCount() : defaultMaxMemberCount)
                .build();

        teamRepository.save(team);

        // 팀장을 팀원으로 자동 추가
        TeamMember leaderMember = TeamMember.builder().team(team).user(leader).roleType(TeamMemberRole.OWNER).build();
        teamMemberRepository.save(leaderMember);
        team.getMembers().add(leaderMember);

        replacePositions(team, request.getPositions());

        // 해커톤 연결된 경우 자동 참가 신청
        if (hackathon != null) {
            HackathonRegistration registration = HackathonRegistration.builder()
                    .hackathon(hackathon)
                    .team(team)
                    .build();
            registrationRepository.save(registration);
        }

        return new TeamDetailResponse(team);
    }

    @Transactional
    public TeamDetailResponse updateTeam(Long teamId, TeamUpdateRequest request, Long userId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new CustomException(ErrorCode.TEAM_NOT_FOUND));

        if (!team.isLeader(userId)) {
            throw new CustomException(ErrorCode.NOT_TEAM_LEADER);
        }

        if (request.getMaxMemberCount() != null && request.getMaxMemberCount() < team.getCurrentMemberCount()) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }

        team.update(request.getName(), request.getDescription(), request.getIsOpen());
        team.updateMaxMemberCount(request.getMaxMemberCount());
        replacePositions(team, request.getPositions());
        return new TeamDetailResponse(team);
    }

    @Transactional
    public void deleteTeam(Long teamId, Long userId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new CustomException(ErrorCode.TEAM_NOT_FOUND));

        if (!team.isLeader(userId)) {
            throw new CustomException(ErrorCode.NOT_TEAM_LEADER);
        }

        if (team.getHackathon() != null && !team.getHackathon().isRegistrationOpen()) {
            throw new CustomException(ErrorCode.TEAM_APPLICATION_CLOSED);
        }

        teamApplicationRepository.deleteAllByTeamId(teamId);

        registrationRepository.findByTeamId(teamId)
                .ifPresent(registrationRepository::delete);

        team.softDelete();
    }

    @Transactional
    public void apply(Long teamId, Long userId, String position) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new CustomException(ErrorCode.TEAM_NOT_FOUND));

        if (teamMemberRepository.existsByTeamIdAndUserId(teamId, userId)) {
            throw new CustomException(ErrorCode.TEAM_ALREADY_EXISTS);
        }

        if (team.getHackathon() != null && !team.getHackathon().isRegistrationOpen()) {
            throw new CustomException(ErrorCode.TEAM_APPLICATION_CLOSED);
        }

        if (team.isFull()) {
            throw new CustomException(ErrorCode.TEAM_FULL);
        }

        if (teamApplicationRepository.existsByTeamIdAndUserIdAndStatus(teamId, userId, ApplicationStatus.PENDING)) {
            throw new CustomException(ErrorCode.ALREADY_APPLIED);
        }

        if (team.getHackathon() != null &&
                teamMemberRepository.existsByUserIdAndHackathonId(userId, team.getHackathon().getId())) {
            throw new CustomException(ErrorCode.TEAM_ALREADY_EXISTS);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        teamApplicationRepository.save(TeamApplication.builder().team(team).user(user).position(position).build());
    }

    @Transactional(readOnly = true)
    public List<TeamApplicationResponse> getApplications(Long teamId, Long userId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new CustomException(ErrorCode.TEAM_NOT_FOUND));

        if (!team.isLeader(userId)) {
            throw new CustomException(ErrorCode.NOT_TEAM_LEADER);
        }

        return teamApplicationRepository.findAllByTeamIdAndStatus(teamId, ApplicationStatus.PENDING)
                .stream().map(TeamApplicationResponse::new).toList();
    }

    @Transactional
    public TeamApplicationResponse decideApplication(Long teamId, Long appId, ApplicationDecisionRequest request, Long userId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new CustomException(ErrorCode.TEAM_NOT_FOUND));

        if (!team.isLeader(userId)) {
            throw new CustomException(ErrorCode.NOT_TEAM_LEADER);
        }

        if (team.isFull()) {
            throw new CustomException(ErrorCode.TEAM_FULL);
        }

        TeamApplication application = teamApplicationRepository.findById(appId)
                .orElseThrow(() -> new CustomException(ErrorCode.APPLICATION_NOT_FOUND));

        User processor = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (request.getStatus() == ApplicationStatus.ACCEPTED) {
            application.accept(processor);
            TeamMember newMember = TeamMember.builder().team(team).user(application.getUser()).roleType(TeamMemberRole.MEMBER).position(application.getPosition()).build();
            teamMemberRepository.save(newMember);
            team.incrementMemberCount();
        } else {
            application.reject(processor);
        }

        return new TeamApplicationResponse(application);
    }

    private void replacePositions(Team team, List<TeamPositionRequest> requestedPositions) {
        if (requestedPositions == null) {
            return;
        }

        List<TeamPosition> nextPositions = new ArrayList<>();
        for (TeamPositionRequest request : requestedPositions) {
            if (request == null || request.getPositionName() == null || request.getPositionName().isBlank()) {
                continue;
            }

            nextPositions.add(
                    TeamPosition.builder()
                            .team(team)
                            .positionName(request.getPositionName().trim())
                            .requiredCount(request.getRequiredCount() != null ? request.getRequiredCount() : 1)
                            .build()
            );
        }

        team.getPositions().clear();
        team.getPositions().addAll(nextPositions);
    }
}
