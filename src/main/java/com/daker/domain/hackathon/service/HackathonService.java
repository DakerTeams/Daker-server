package com.daker.domain.hackathon.service;

import com.daker.domain.hackathon.domain.*;
import com.daker.domain.hackathon.dto.*;
import com.daker.domain.hackathon.repository.*;
import com.daker.domain.team.domain.Team;
import com.daker.domain.team.repository.TeamMemberRepository;
import com.daker.domain.team.repository.TeamRepository;
import com.daker.domain.judge.repository.JudgeEvaluationRepository;
import com.daker.domain.submission.repository.SubmissionRepository;
import com.daker.domain.vote.repository.VoteRepository;
import com.daker.global.exception.CustomException;
import com.daker.global.exception.ErrorCode;
import com.daker.global.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HackathonService {

    private final HackathonRepository hackathonRepository;
    private final HackathonTagRepository hackathonTagRepository;
    private final HackathonRegistrationRepository registrationRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final VoteRepository voteRepository;
    private final SubmissionRepository submissionRepository;
    private final JudgeEvaluationRepository judgeEvaluationRepository;

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
                .filter(t -> t.getHackathon() != null && t.getHackathon().getId().equals(hackathonId))
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

        if (hackathon.getScoreType() == ScoreType.VOTE) {
            return buildVoteLeaderboard(hackathon, teams);
        }

        return buildScoreLeaderboard(hackathon, teams);
    }

    private LeaderboardResponse buildVoteLeaderboard(Hackathon hackathon, List<Team> teams) {
        // 해커톤 종료 전에는 결과 비공개 — rank/score 모두 null
        if (!hackathon.isEnded()) {
            List<LeaderboardResponse.LeaderboardTeamInfo> items = teams.stream()
                    .map(team -> new LeaderboardResponse.LeaderboardTeamInfo(team, null, null, false))
                    .toList();
            return new LeaderboardResponse(ScoreType.VOTE.name(), items);
        }

        // 팀별 득표수 집계
        Map<Long, Long> voteCountByTeam = voteRepository
                .countByHackathonIdGroupByTeam(hackathon.getId())
                .stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));

        // 득표수 내림차순 정렬 후 rank 부여, score는 null (득표수 비공개)
        List<Team> sorted = teams.stream()
                .sorted(Comparator.comparingLong(
                        (Team t) -> voteCountByTeam.getOrDefault(t.getId(), 0L)
                ).reversed())
                .toList();

        List<LeaderboardResponse.LeaderboardTeamInfo> items = new java.util.ArrayList<>();
        for (int i = 0; i < sorted.size(); i++) {
            items.add(new LeaderboardResponse.LeaderboardTeamInfo(sorted.get(i), i + 1, null, false));
        }

        return new LeaderboardResponse(ScoreType.VOTE.name(), items);
    }

    private LeaderboardResponse buildScoreLeaderboard(Hackathon hackathon, List<Team> teams) {
        boolean ended = hackathon.isEnded()
                || hackathon.getStatus() == HackathonStatus.CLOSED
                || hackathon.getStatus() == HackathonStatus.ENDED;

        // 제출 여부 맵
        Map<Long, Boolean> submittedByTeam = teams.stream()
                .collect(Collectors.toMap(
                        Team::getId,
                        t -> submissionRepository
                                .findByTeamIdAndHackathonIdAndIsLatestTrue(t.getId(), hackathon.getId())
                                .isPresent()
                ));

        if (!ended) {
            // 진행 중: 제출 여부만 표시, 점수/순위 비공개
            List<LeaderboardResponse.LeaderboardTeamInfo> items = teams.stream()
                    .map(t -> new LeaderboardResponse.LeaderboardTeamInfo(
                            t, null, null, submittedByTeam.getOrDefault(t.getId(), false)))
                    .toList();
            return new LeaderboardResponse(ScoreType.SCORE.name(), items);
        }

        // 종료 후: 팀별 심사 평균 점수 계산 후 순위 부여
        Map<Long, Double> avgScoreByTeam = new java.util.HashMap<>();
        for (Team team : teams) {
            List<com.daker.domain.judge.domain.JudgeEvaluation> evals =
                    judgeEvaluationRepository.findAllByHackathonIdAndTeamId(hackathon.getId(), team.getId());
            if (!evals.isEmpty()) {
                double avg = evals.stream().mapToDouble(e -> e.getTotalScore()).average().orElse(0);
                avgScoreByTeam.put(team.getId(), avg);
            }
        }

        List<Team> sorted = teams.stream()
                .sorted(Comparator.comparingDouble(
                        (Team t) -> avgScoreByTeam.getOrDefault(t.getId(), -1.0)
                ).reversed())
                .toList();

        List<LeaderboardResponse.LeaderboardTeamInfo> items = new java.util.ArrayList<>();
        for (int i = 0; i < sorted.size(); i++) {
            Team t = sorted.get(i);
            Double score = avgScoreByTeam.get(t.getId());
            int rank = score != null ? i + 1 : null != null ? 0 : 0;
            items.add(new LeaderboardResponse.LeaderboardTeamInfo(
                    t,
                    score != null ? i + 1 : null,
                    score,
                    submittedByTeam.getOrDefault(t.getId(), false)
            ));
        }

        return new LeaderboardResponse(ScoreType.SCORE.name(), items);
    }
}
