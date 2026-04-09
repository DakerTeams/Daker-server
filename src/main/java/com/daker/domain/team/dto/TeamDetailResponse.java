package com.daker.domain.team.dto;

import com.daker.domain.team.domain.Team;
import com.daker.domain.team.domain.TeamMember;
import com.daker.domain.team.domain.TeamMemberRole;
import com.daker.domain.team.domain.TeamPrivateInfo;
import com.daker.domain.team.domain.TeamStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class TeamDetailResponse {

    private final Long id;
    private final Long hackathonId;
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
    private final TeamSummaryResponse.LeaderInfo leader;
    private final List<MemberInfo> members;
    private final List<TeamSummaryResponse.PositionInfo> positions;
    private final ContactInfo contact;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public TeamDetailResponse(Team team, TeamPrivateInfo privateInfo) {
        this.id = team.getId();
        this.hackathonId = team.getHackathon() != null ? team.getHackathon().getId() : null;
        this.name = team.getName();
        this.description = team.getDescription();
        this.status = team.getStatus();
        this.isOpen = team.isOpen();
        this.isPublic = team.isPublic();
        this.isDeleted = team.getDeletedAt() != null;
        this.currentMemberCount = team.getCurrentMemberCount();
        this.maxMemberCount = team.getMaxMemberCount();
        this.leader = new TeamSummaryResponse.LeaderInfo(team.getLeader().getId(), team.getLeader().getNickname());
        this.members = team.getMembers().stream().map(MemberInfo::new).toList();
        this.positions = team.getPositions().stream()
                .map(p -> new TeamSummaryResponse.PositionInfo(p.getPositionName(), p.getRequiredCount()))
                .toList();
        this.contact = privateInfo != null ? new ContactInfo(privateInfo.getContactType(), privateInfo.getContactValue()) : null;
        this.createdAt = team.getCreatedAt();
        this.updatedAt = team.getUpdatedAt();
    }

    public TeamDetailResponse(Team team) {
        this(team, null);
    }

    @Getter
    public static class ContactInfo {
        private final String type;
        private final String value;

        public ContactInfo(String type, String value) {
            this.type = type;
            this.value = value;
        }
    }

    @Getter
    public static class MemberInfo {
        private final Long userId;
        private final String nickname;
        private final TeamMemberRole roleType;
        private final String position;

        public MemberInfo(TeamMember member) {
            this.userId = member.getUser().getId();
            this.nickname = member.getUser().getNickname();
            this.roleType = member.getRoleType();
            this.position = member.getPosition();
        }
    }
}
