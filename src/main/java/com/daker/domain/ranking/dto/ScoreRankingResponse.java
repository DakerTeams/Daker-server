package com.daker.domain.ranking.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ScoreRankingResponse {

    private final Long userId;
    private final int rank;
    private final String nickname;
    private final int score;
    private final int participationCount;
    private final int completedCount;
    private final String submitRate;
    private final String bestRank;
    private final boolean isMe;
}
