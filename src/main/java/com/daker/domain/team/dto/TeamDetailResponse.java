package com.daker.domain.team.dto;

import com.daker.domain.team.domain.Team;
import com.daker.domain.team.domain.TeamMember;
import lombok.Getter;

import java.util.List;

@Getter
public class TeamDetailResponse {

    private final Long id;
    private final Long hackathonId;
    private final String name;
    private final String description;
    private final boolean isOpen;
    private final TeamSummaryResponse.LeaderInfo leader;
    private final List<MemberInfo> members;

    public TeamDetailResponse(Team team) {
        this.id = team.getId();
        this.hackathonId = team.getHackathon().getId();
        this.name = team.getName();
        this.description = team.getDescription();
        this.isOpen = team.isOpen();
        this.leader = new TeamSummaryResponse.LeaderInfo(team.getLeader().getId(), team.getLeader().getNickname());
        this.members = team.getMembers().stream().map(MemberInfo::new).toList();
    }

    @Getter
    public static class MemberInfo {
        private final Long userId;
        private final String nickname;

        public MemberInfo(TeamMember member) {
            this.userId = member.getUser().getId();
            this.nickname = member.getUser().getNickname();
        }
    }
}
