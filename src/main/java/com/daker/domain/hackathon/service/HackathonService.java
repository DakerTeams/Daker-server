package com.daker.domain.hackathon.service;

import com.daker.domain.hackathon.domain.*;
import com.daker.domain.hackathon.dto.*;
import com.daker.domain.hackathon.repository.*;
import com.daker.domain.team.domain.Team;
import com.daker.domain.team.repository.TeamMemberRepository;
import com.daker.domain.team.repository.TeamRepository;
import com.daker.global.exception.CustomException;
import com.daker.global.exception.ErrorCode;
import com.daker.global.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HackathonService {

    private final HackathonRepository hackathonRepository;
    private final HackathonTagRepository hackathonTagRepository;
    private final HackathonRegistrationRepository registrationRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;

    @Transactional(readOnly = true)
    public PageResponse<HackathonSummaryResponse> getHackathons(HackathonStatus status, String tag, String q, Pageable pageable) {
        Page<Hackathon> hackathons = hackathonRepository.findAllWithFilters(status, tag, q, pageable);

        Page<HackathonSummaryResponse> result = hackathons.map(hackathon -> {
            List<String> tags = hackathonTagRepository.findAllByHackathonId(hackathon.getId())
                    .stream().map(ht -> ht.getTag().getName()).toList();

            int participants = teamRepository.findAllByHackathonId(hackathon.getId()).stream()
                    .mapToInt(t -> t.getMembers().size()).sum();

            return new HackathonSummaryResponse(hackathon, tags, participants);
        });

        return new PageResponse<>(result);
    }

    @Transactional(readOnly = true)
    public HackathonDetailResponse getHackathon(Long hackathonId) {
        Hackathon hackathon = hackathonRepository.findByIdAndDeletedFalse(hackathonId)
                .orElseThrow(() -> new CustomException(ErrorCode.HACKATHON_NOT_FOUND));

        List<String> tags = hackathonTagRepository.findAllByHackathonId(hackathonId)
                .stream().map(ht -> ht.getTag().getName()).toList();

        int participants = teamRepository.findAllByHackathonId(hackathonId).stream()
                .mapToInt(t -> t.getMembers().size()).sum();

        return new HackathonDetailResponse(hackathon, tags, participants);
    }

    @Transactional(readOnly = true)
    public RegistrationStatusResponse getRegistrationStatus(Long hackathonId, Long userId) {
        hackathonRepository.findByIdAndDeletedFalse(hackathonId)
                .orElseThrow(() -> new CustomException(ErrorCode.HACKATHON_NOT_FOUND));

        return teamRepository.findAllByUserId(userId).stream()
                .filter(t -> t.getHackathon().getId().equals(hackathonId))
                .findFirst()
                .flatMap(t -> registrationRepository.findByTeamId(t.getId()))
                .map(RegistrationStatusResponse::new)
                .orElse(RegistrationStatusResponse.notRegistered());
    }

    @Transactional
    public RegistrationStatusResponse register(Long hackathonId, Long userId, RegistrationRequest request) {
        Hackathon hackathon = hackathonRepository.findByIdAndDeletedFalse(hackathonId)
                .orElseThrow(() -> new CustomException(ErrorCode.HACKATHON_NOT_FOUND));

        if (!hackathon.isRegistrationOpen()) {
            throw new CustomException(ErrorCode.REGISTRATION_PERIOD_INVALID);
        }

        Team team = teamRepository.findById(request.getTeamId())
                .orElseThrow(() -> new CustomException(ErrorCode.TEAM_NOT_FOUND));

        if (!team.isLeader(userId)) {
            throw new CustomException(ErrorCode.NOT_TEAM_LEADER);
        }

        if (registrationRepository.existsByHackathonIdAndTeamId(hackathonId, team.getId())) {
            throw new CustomException(ErrorCode.ALREADY_APPLIED);
        }

        // 독립 팀인 경우 해커톤 연결
        if (team.getHackathon() == null) {
            // 해당 해커톤에 이미 다른 팀으로 참여 중인지 확인
            boolean alreadyInHackathon = teamMemberRepository.existsByUserIdAndHackathonId(userId, hackathonId);
            if (alreadyInHackathon) {
                throw new CustomException(ErrorCode.TEAM_ALREADY_EXISTS);
            }
            team.linkHackathon(hackathon);
        }

        HackathonRegistration registration = HackathonRegistration.builder()
                .hackathon(hackathon)
                .team(team)
                .build();
        registrationRepository.save(registration);

        return new RegistrationStatusResponse(registration);
    }

    @Transactional
    public void cancelRegistration(Long hackathonId, Long userId) {
        Hackathon hackathon = hackathonRepository.findByIdAndDeletedFalse(hackathonId)
                .orElseThrow(() -> new CustomException(ErrorCode.HACKATHON_NOT_FOUND));

        if (!hackathon.isRegistrationOpen()) {
            throw new CustomException(ErrorCode.REGISTRATION_PERIOD_INVALID);
        }

        Team myTeam = teamRepository.findAllByUserId(userId).stream()
                .filter(t -> t.getHackathon().getId().equals(hackathonId))
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.REGISTRATION_NOT_FOUND));

        if (!myTeam.isLeader(userId)) {
            throw new CustomException(ErrorCode.NOT_TEAM_LEADER);
        }

        HackathonRegistration registration = registrationRepository.findByTeamId(myTeam.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.REGISTRATION_NOT_FOUND));

        registrationRepository.delete(registration);
    }

    @Transactional(readOnly = true)
    public PageResponse<HackathonSummaryResponse> getHackathonTeams(Long hackathonId, Pageable pageable) {
        hackathonRepository.findByIdAndDeletedFalse(hackathonId)
                .orElseThrow(() -> new CustomException(ErrorCode.HACKATHON_NOT_FOUND));

        // 팀 목록은 TeamService에서 처리 — 여기선 해커톤 유효성만 검증
        throw new UnsupportedOperationException("TeamService.getTeams() 사용");
    }

    @Transactional(readOnly = true)
    public LeaderboardResponse getLeaderboard(Long hackathonId) {
        Hackathon hackathon = hackathonRepository.findByIdAndDeletedFalse(hackathonId)
                .orElseThrow(() -> new CustomException(ErrorCode.HACKATHON_NOT_FOUND));

        List<Team> teams = teamRepository.findAllByHackathonId(hackathonId);

        // TODO: 제출 도메인 개발 후 submitted 여부 및 score 연결
        List<LeaderboardResponse.LeaderboardTeamInfo> items = teams.stream()
                .map(team -> new LeaderboardResponse.LeaderboardTeamInfo(team, null, null, false))
                .toList();

        return new LeaderboardResponse(hackathon.getScoreType().name(), items);
    }
}
