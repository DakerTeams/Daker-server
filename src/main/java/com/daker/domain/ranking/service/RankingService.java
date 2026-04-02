package com.daker.domain.ranking.service;

import com.daker.domain.ranking.dto.ParticipationRankingResponse;
import com.daker.domain.ranking.dto.RankingPeriod;
import com.daker.domain.ranking.dto.ScoreRankingResponse;
import com.daker.domain.team.domain.TeamMember;
import com.daker.domain.team.repository.TeamMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RankingService {

    private final TeamMemberRepository teamMemberRepository;

    @Transactional(readOnly = true)
    public List<ScoreRankingResponse> getScoreRankings(RankingPeriod period, Long userId) {
        List<ParticipationAccumulator> sorted = buildAccumulators(period).values().stream()
                .sorted(Comparator
                        .comparingInt(ParticipationAccumulator::getScore).reversed()
                        .thenComparingInt(ParticipationAccumulator::getParticipationCount).reversed()
                        .thenComparingInt(ParticipationAccumulator::getCompletedCount).reversed()
                        .thenComparing(ParticipationAccumulator::getNickname))
                .toList();

        return buildScoreResponses(sorted, userId);
    }

    @Transactional(readOnly = true)
    public List<ParticipationRankingResponse> getParticipationRankings(RankingPeriod period, Long userId) {
        List<ParticipationAccumulator> sorted = buildAccumulators(period).values().stream()
                .sorted(Comparator
                        .comparingInt(ParticipationAccumulator::getParticipationCount).reversed()
                        .thenComparingInt(ParticipationAccumulator::getCompletedCount).reversed()
                        .thenComparingDouble(ParticipationAccumulator::getSubmitRateValue).reversed()
                        .thenComparing(ParticipationAccumulator::getNickname))
                .toList();

        return buildParticipationResponses(sorted, userId);
    }

    private Map<Long, ParticipationAccumulator> buildAccumulators(RankingPeriod period) {
        LocalDateTime startDateTime = period.resolveStartDateTime();
        Map<Long, ParticipationAccumulator> accumulators = new HashMap<>();

        for (TeamMember member : teamMemberRepository.findAllActiveMembersForRanking()) {
            if (startDateTime != null && member.getTeam().getHackathon().getStartDate().isBefore(startDateTime)) {
                continue;
            }

            Long userIdKey = member.getUser().getId();
            accumulators.computeIfAbsent(
                    userIdKey,
                    ignored -> new ParticipationAccumulator(userIdKey, member.getUser().getNickname())
            ).addParticipation(member);
        }

        return accumulators;
    }

    private List<ScoreRankingResponse> buildScoreResponses(
            List<ParticipationAccumulator> sorted,
            Long currentUserId
    ) {
        java.util.ArrayList<ScoreRankingResponse> responses = new java.util.ArrayList<>();

        for (int index = 0; index < sorted.size(); index++) {
            ParticipationAccumulator item = sorted.get(index);
            responses.add(new ScoreRankingResponse(
                    item.getUserId(),
                    index + 1,
                    item.getNickname(),
                    item.getScore(),
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
        java.util.ArrayList<ParticipationRankingResponse> responses = new java.util.ArrayList<>();

        for (int index = 0; index < sorted.size(); index++) {
            ParticipationAccumulator item = sorted.get(index);
            responses.add(new ParticipationRankingResponse(
                    item.getUserId(),
                    index + 1,
                    item.getNickname(),
                    item.getParticipationCount(),
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

        private void addParticipation(TeamMember member) {
            Long hackathonId = member.getTeam().getHackathon().getId();
            participatedHackathonIds.add(hackathonId);

            if (isCompleted(member)) {
                completedHackathonIds.add(hackathonId);
            }
        }

        private boolean isCompleted(TeamMember member) {
            return member.getTeam().getHackathon().isEnded()
                    || member.getTeam().getHackathon().getStatus() == com.daker.domain.hackathon.domain.HackathonStatus.CLOSED
                    || member.getTeam().getHackathon().getStatus() == com.daker.domain.hackathon.domain.HackathonStatus.ENDED;
        }

        private Long getUserId() {
            return userId;
        }

        private String getNickname() {
            return nickname;
        }

        private int getScore() {
            // TODO: 제출/심사 도메인 연동 후 실제 점수 기반 랭킹으로 교체
            return (getCompletedCount() * 1000) + (getParticipationCount() * 100);
        }

        private int getParticipationCount() {
            return participatedHackathonIds.size();
        }

        private int getCompletedCount() {
            return completedHackathonIds.size();
        }

        private double getSubmitRateValue() {
            if (getParticipationCount() == 0) {
                return 0;
            }
            return ((double) getCompletedCount() / getParticipationCount()) * 100;
        }

        private String getSubmitRateLabel() {
            return Math.round(getSubmitRateValue()) + "%";
        }
    }
}
