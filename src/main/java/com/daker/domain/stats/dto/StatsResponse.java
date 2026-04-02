package com.daker.domain.stats.dto;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class StatsResponse {

    private final long participants;
    private final long activeHackathons;
    private final long totalPrize;
    private final LocalDateTime updatedAt;

    public StatsResponse(long participants, long activeHackathons, long totalPrize) {
        this.participants = participants;
        this.activeHackathons = activeHackathons;
        this.totalPrize = totalPrize;
        this.updatedAt = LocalDateTime.now();
    }
}