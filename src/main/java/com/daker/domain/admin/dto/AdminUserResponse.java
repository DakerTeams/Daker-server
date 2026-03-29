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
    private final LocalDateTime createdAt;

    public AdminUserResponse(User user) {
        this.userId = user.getId();
        this.nickname = user.getNickname();
        this.email = user.getEmail();
        this.role = user.getRole();
        this.createdAt = user.getCreatedAt();
    }
}
