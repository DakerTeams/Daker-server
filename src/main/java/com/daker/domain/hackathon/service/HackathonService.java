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

        Team myTeam = teamRepository.findAllByUserId(userId).stream()
                .filter(t -> t.getHackathon().getId().equals(hackathonId))
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.REGISTRATION_NOT_FOUND));

        HackathonRegistration registration = registrationRepository.findByTeamId(myTeam.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.REGISTRATION_NOT_FOUND));

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
        boolean isEnded = hackathon.isEnded();

        List<LeaderboardResponse.LeaderboardTeamInfo> teamInfos = teams.stream()
                .map(team -> new LeaderboardResponse.LeaderboardTeamInfo(team, isEnded ? null : null))
                .toList();

        return new LeaderboardResponse(isEnded, teamInfos);
    }
}
