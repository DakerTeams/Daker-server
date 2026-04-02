package com.daker.domain.auth.dto;

import com.daker.domain.user.domain.AccountStatus;
import com.daker.domain.user.domain.Role;
import com.daker.domain.user.domain.User;
import com.daker.domain.user.domain.UserTag;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class MeResponse {

    private final Long userId;
    private final String nickname;
    private final String email;
    private final Role role;
    private final AccountStatus accountStatus;
    private final int points;
    private final int rank;
    private final int joinedHackathons;
    private final List<TagInfo> tags;
    private final LocalDateTime createdAt;

    public MeResponse(User user, int joinedHackathons, int points, int rank, List<UserTag> userTags) {
        this.userId = user.getId();
        this.nickname = user.getNickname();
        this.email = user.getEmail();
        this.role = user.getRole();
        this.accountStatus = user.getAccountStatus();
        this.points = points;
        this.rank = rank;
        this.joinedHackathons = joinedHackathons;
        this.tags = userTags.stream().map(TagInfo::new).toList();
        this.createdAt = user.getCreatedAt();
    }

    @Getter
    public static class TagInfo {
        private final Long tagId;
        private final String name;

        public TagInfo(UserTag userTag) {
            this.tagId = userTag.getTag().getId();
            this.name = userTag.getTag().getName();
        }
    }
}
