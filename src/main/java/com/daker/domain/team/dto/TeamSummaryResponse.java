package com.daker.domain.team.dto;

import com.daker.domain.team.domain.Team;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class TeamSummaryResponse {

    private final Long id;
    private final Long hackathonId;
    private final String name;
    private final String description;
    @JsonProperty("isOpen")
    private final boolean isOpen;
    private final int memberCount;
    private final LeaderInfo leader;

    public TeamSummaryResponse(Team team) {
        this.id = team.getId();
        this.hackathonId = team.getHackathon().getId();
        this.name = team.getName();
        this.description = team.getDescription();
        this.isOpen = team.isOpen();
        this.memberCount = team.getMembers().size();
        this.leader = new LeaderInfo(team.getLeader().getId(), team.getLeader().getNickname());
    }

    @Getter
    public static class LeaderInfo {
        private final Long userId;
        private final String nickname;

        public LeaderInfo(Long userId, String nickname) {
            this.userId = userId;
            this.nickname = nickname;
        }
    }
}
