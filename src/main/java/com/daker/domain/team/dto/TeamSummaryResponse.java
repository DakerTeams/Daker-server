package com.daker.domain.team.dto;

import com.daker.domain.team.domain.Team;
import com.daker.domain.team.domain.TeamStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
public class TeamSummaryResponse {

    private final Long id;
    private final String hackathonTitle;
    private final String name;
    private final String description;
    private final TeamStatus status;
    @JsonProperty("isOpen")
    private final boolean isOpen;
    @JsonProperty("isPublic")
    private final boolean isPublic;
    @JsonProperty("isDeleted")
    private final boolean isDeleted;
    private final int currentMemberCount;
    private final int maxMemberCount;
    private final LeaderInfo leader;
    private final List<PositionInfo> positions;

    public TeamSummaryResponse(Team team) {
        this.id = team.getId();
        this.hackathonTitle = team.getHackathon() != null ? team.getHackathon().getTitle() : null;
        this.name = team.getName();
        this.description = team.getDescription();
        this.status = team.getStatus();
        this.isOpen = team.isOpen();
        this.isPublic = team.isPublic();
        this.isDeleted = team.getDeletedAt() != null;
        this.currentMemberCount = team.getCurrentMemberCount();
        this.maxMemberCount = team.getMaxMemberCount();
        this.leader = new LeaderInfo(team.getLeader().getId(), team.getLeader().getNickname());
        this.positions = team.getPositions().stream()
                .map(p -> new PositionInfo(p.getPositionName(), p.getRequiredCount()))
                .toList();
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

    @Getter
    public static class PositionInfo {
        private final String positionName;
        private final int requiredCount;

        public PositionInfo(String positionName, int requiredCount) {
            this.positionName = positionName;
            this.requiredCount = requiredCount;
        }
    }
}
