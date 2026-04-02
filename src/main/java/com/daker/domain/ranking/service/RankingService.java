package com.daker.domain.ranking.service;

import com.daker.domain.hackathon.domain.HackathonStatus;
import com.daker.domain.ranking.dto.MyRankingResponse;
import com.daker.domain.ranking.dto.ParticipationRankingResponse;
import com.daker.domain.ranking.dto.RankingPeriod;
import com.daker.domain.ranking.dto.ScoreRankingResponse;
import com.daker.domain.submission.repository.SubmissionRepository;
import com.daker.domain.team.domain.TeamMember;
import com.daker.domain.team.repository.TeamMemberRepository;
import com.daker.domain.xp.repository.UserXpHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RankingService {

    private final TeamMemberRepository teamMemberRepository;
    private final UserXpHistoryRepository userXpHistoryRepository;
    private final SubmissionRepository submissionRepository;

    @Transactional(readOnly = true)
    public List<ScoreRankingResponse> getScoreRankings(RankingPeriod period, Long userId) {
        Map<Long, Integer> xpByUser = loadXpMap();
        List<ParticipationAccumulator> accs = buildAccumulators(period).values().stream().toList();

        List<ParticipationAccumulator> sorted = accs.stream()
                .sorted(Comparator
                        .comparingInt((ParticipationAccumulator a) -> xpByUser.getOrDefault(a.getUserId(), 0)).reversed()
                        .thenComparing(ParticipationAccumulator::getNickname))
                .toList();

        return buildScoreResponses(sorted, xpByUser, userId);
    }

    @Transactional(readOnly = true)
    public List<ParticipationRankingResponse> getParticipationRankings(RankingPeriod period, Long userId) {
        List<ParticipationAccumulator> sorted = buildAccumulators(period).values().stream()
                .sorted(Comparator
                        .comparingInt(ParticipationAccumulator::getCompletedCount).reversed()
                        .thenComparingInt(ParticipationAccumulator::getParticipationCount).reversed()
                        .thenComparing(ParticipationAccumulator::getNickname))
                .toList();

        return buildParticipationResponses(sorted, userId);
    }

    @Transactional(readOnly = true)
    public MyRankingResponse getMyRanking(RankingPeriod period, Long userId) {
        Map<Long, Integer> xpByUser = loadXpMap();
        Map<Long, ParticipationAccumulator> accMap = buildAccumulators(period);

        List<ParticipationAccumulator> scoreSorted = accMap.values().stream()
                .sorted(Comparator
                        .comparingInt((ParticipationAccumulator a) -> xpByUser.getOrDefault(a.getUserId(), 0)).reversed()
                        .thenComparing(ParticipationAccumulator::getNickname))
                .toList();

        List<ParticipationAccumulator> participationSorted = accMap.values().stream()
                .sorted(Comparator
                        .comparingInt(ParticipationAccumulator::getCompletedCount).reversed()
                        .thenComparingInt(ParticipationAccumulator::getParticipationCount).reversed()
                        .thenComparing(ParticipationAccumulator::getNickname))
                .toList();

        int scoreRank = 1;
        int scorePoints = 0;
        for (int i = 0; i < scoreSorted.size(); i++) {
            if (scoreSorted.get(i).getUserId().equals(userId)) {
                scoreRank = i + 1;
                scorePoints = xpByUser.getOrDefault(userId, 0);
                break;
            }
        }

        int participationRank = 1;
        int hackathonCount = 0;
        int completionCount = 0;
        for (int i = 0; i < participationSorted.size(); i++) {
            ParticipationAccumulator acc = participationSorted.get(i);
            if (acc.getUserId().equals(userId)) {
                participationRank = i + 1;
                hackathonCount = acc.getParticipationCount();
                completionCount = acc.getCompletedCount();
                break;
            }
        }

        return new MyRankingResponse(
                new MyRankingResponse.ScoreRank(scoreRank, scorePoints),
                new MyRankingResponse.ParticipationRank(participationRank, hackathonCount, completionCount,
                        participationSorted.stream().filter(a -> a.getUserId().equals(userId))
                                .findFirst().map(ParticipationAccumulator::getSubmitRateLabel).orElse("0%"))
        );
    }

    // XP 합산 맵 로드 (userId → totalXp)
    private Map<Long, Integer> loadXpMap() {
        return userXpHistoryRepository.sumXpGroupByUser().stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> ((Number) row[1]).intValue()
                ));
    }

    private Map<Long, ParticipationAccumulator> buildAccumulators(RankingPeriod period) {
        LocalDateTime startDateTime = period.resolveStartDateTime();

        // 제출물이 있는 (hackathonId, teamId) 쌍 사전 로드
        Set<String> submittedPairs = submissionRepository.findAllLatestHackathonTeamPairs().stream()
                .map(row -> row[0] + ":" + row[1])
                .collect(Collectors.toSet());

        Map<Long, ParticipationAccumulator> accumulators = new HashMap<>();

        for (TeamMember member : teamMemberRepository.findAllActiveMembersForRanking()) {
            if (startDateTime != null && member.getJoinedAt().isBefore(startDateTime)) {
                continue;
            }

            Long userIdKey = member.getUser().getId();
            accumulators.computeIfAbsent(
                    userIdKey,
                    ignored -> new ParticipationAccumulator(userIdKey, member.getUser().getNickname())
            ).addParticipation(member, submittedPairs);
        }

        return accumulators;
    }

    private List<ScoreRankingResponse> buildScoreResponses(
            List<ParticipationAccumulator> sorted,
            Map<Long, Integer> xpByUser,
            Long currentUserId
    ) {
        List<ScoreRankingResponse> responses = new ArrayList<>();
        for (int i = 0; i < sorted.size(); i++) {
            ParticipationAccumulator item = sorted.get(i);
            responses.add(new ScoreRankingResponse(
                    item.getUserId(),
                    i + 1,
                    item.getNickname(),
                    xpByUser.getOrDefault(item.getUserId(), 0),
                    item.getParticipationCount(),
                    item.getCompletedCount(),
                    item.getSubmitRateLabel(),
                    "미집계",
                    item.getUserId().equals(currentUserId)
            ));
        }
        return responses;
    }

    private List<ParticipationRankingResponse> buildParticipationResponses(
            List<ParticipationAccumulator> sorted,
            Long currentUserId
    ) {
        List<ParticipationRankingResponse> responses = new ArrayList<>();
        for (int i = 0; i < sorted.size(); i++) {
            ParticipationAccumulator item = sorted.get(i);
            responses.add(new ParticipationRankingResponse(
                    item.getUserId(),
                    i + 1,
                    item.getNickname(),
                    item.getCompletedCount(),      // 완주 횟수를 참가 횟수로
                    item.getCompletedCount(),
                    item.getSubmitRateLabel(),
                    item.getUserId().equals(currentUserId)
            ));
        }
        return responses;
    }

    private static class ParticipationAccumulator {

        private final Long userId;
        private final String nickname;
        private final Set<Long> participatedHackathonIds = new HashSet<>();
        private final Set<Long> completedHackathonIds = new HashSet<>();

        private ParticipationAccumulator(Long userId, String nickname) {
            this.userId = userId;
            this.nickname = nickname;
        }

        private void addParticipation(TeamMember member, Set<String> submittedPairs) {
            Long hackathonId = member.getTeam().getHackathon().getId();
            participatedHackathonIds.add(hackathonId);

            if (isCompleted(member, submittedPairs)) {
                completedHackathonIds.add(hackathonId);
            }
        }

        // 완주 = 해커톤 종료(CLOSED/ENDED) + 팀 제출물 존재
        private boolean isCompleted(TeamMember member, Set<String> submittedPairs) {
            HackathonStatus status = member.getTeam().getHackathon().getStatus();
            boolean hackathonEnded = member.getTeam().getHackathon().isEnded()
                    || status == HackathonStatus.CLOSED
                    || status == HackathonStatus.ENDED;

            String pair = member.getTeam().getHackathon().getId() + ":" + member.getTeam().getId();
            return hackathonEnded && submittedPairs.contains(pair);
        }

        private Long getUserId() { return userId; }
        private String getNickname() { return nickname; }
        private int getParticipationCount() { return participatedHackathonIds.size(); }
        private int getCompletedCount() { return completedHackathonIds.size(); }

        private double getSubmitRateValue() {
            if (getParticipationCount() == 0) return 0;
            return ((double) getCompletedCount() / getParticipationCount()) * 100;
        }

        private String getSubmitRateLabel() {
            return Math.round(getSubmitRateValue()) + "%";
        }
    }
}
