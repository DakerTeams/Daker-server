package com.daker.domain.hackathon.dto;

import com.daker.domain.team.domain.Team;
import lombok.Getter;

import java.util.List;

@Getter
public class LeaderboardResponse {

    private final boolean public_;
    private final List<LeaderboardTeamInfo> teams;

    public LeaderboardResponse(boolean isPublic, List<LeaderboardTeamInfo> teams) {
        this.public_ = isPublic;
        this.teams = teams;
    }

    @Getter
    public static class LeaderboardTeamInfo {
        private final Long teamId;
        private final String teamName;
        private final int memberCount;
        private final Double totalScore; // 공개 전 null

        public LeaderboardTeamInfo(Team team, Double totalScore) {
            this.teamId = team.getId();
            this.teamName = team.getName();
            this.memberCount = team.getMembers().size();
            this.totalScore = totalScore;
        }
    }
}
