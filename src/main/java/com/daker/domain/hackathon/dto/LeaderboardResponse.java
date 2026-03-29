package com.daker.domain.hackathon.dto;

import com.daker.domain.team.domain.Team;
import lombok.Getter;

import java.util.List;

@Getter
public class LeaderboardResponse {

    private final String scoreType;
    private final List<LeaderboardTeamInfo> items;

    public LeaderboardResponse(String scoreType, List<LeaderboardTeamInfo> items) {
        this.scoreType = scoreType;
        this.items = items;
    }

    @Getter
    public static class LeaderboardTeamInfo {
        private final Integer rank;
        private final String teamName;
        private final Double score;
        private final boolean submitted;

        public LeaderboardTeamInfo(Team team, Integer rank, Double score, boolean submitted) {
            this.rank = rank;
            this.teamName = team.getName();
            this.score = score;
            this.submitted = submitted;
        }
    }
}
