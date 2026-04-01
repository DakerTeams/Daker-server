package com.daker.domain.stats.dto;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class StatsResponse {

    private final long totalParticipants;
    private final long activeHackathons;
    private final long totalPrizeKRW;
    private final LocalDateTime updatedAt;

    public StatsResponse(long totalParticipants, long activeHackathons, long totalPrizeKRW) {
        this.totalParticipants = totalParticipants;
        this.activeHackathons = activeHackathons;
        this.totalPrizeKRW = totalPrizeKRW;
        this.updatedAt = LocalDateTime.now();
    }
}