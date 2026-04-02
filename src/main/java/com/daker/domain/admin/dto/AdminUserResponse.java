package com.daker.domain.admin.dto;

import com.daker.domain.user.domain.Role;
import com.daker.domain.user.domain.User;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class AdminUserResponse {

    private final Long userId;
    private final String nickname;
    private final String email;
    private final Role role;
    private final int points;
    private final int joinedHackathons;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public AdminUserResponse(User user) {
        this.userId = user.getId();
        this.nickname = user.getNickname();
        this.email = user.getEmail();
        this.role = user.getRole();
        this.points = 0;
        this.joinedHackathons = 0;
        this.createdAt = user.getCreatedAt();
        this.updatedAt = user.getUpdatedAt();
    }

    public AdminUserResponse(User user, int points, int joinedHackathons) {
        this.userId = user.getId();
        this.nickname = user.getNickname();
        this.email = user.getEmail();
        this.role = user.getRole();
        this.points = points;
        this.joinedHackathons = joinedHackathons;
        this.createdAt = user.getCreatedAt();
        this.updatedAt = user.getUpdatedAt();
    }
}
