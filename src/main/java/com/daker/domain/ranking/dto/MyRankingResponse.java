package com.daker.domain.ranking.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class MyRankingResponse {

    private final ScoreRank scoreRank;
    private final ParticipationRank participationRank;

    @Getter
    @RequiredArgsConstructor
    public static class ScoreRank {
        private final int rank;
        private final int points;
    }

    @Getter
    @RequiredArgsConstructor
    public static class ParticipationRank {
        private final int rank;
        private final int hackathonCount;
        private final int completionCount;
        private final String submissionRate;
    }
}
