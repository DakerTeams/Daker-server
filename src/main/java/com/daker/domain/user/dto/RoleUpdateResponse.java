package com.daker.domain.user.dto;

import com.daker.domain.user.domain.Role;
import com.daker.domain.user.domain.User;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class RoleUpdateResponse {

    private final Long userId;
    private final String nickname;
    private final Role role;
    private final LocalDateTime updatedAt;

    public RoleUpdateResponse(User user) {
        this.userId = user.getId();
        this.nickname = user.getNickname();
        this.role = user.getRole();
        this.updatedAt = user.getUpdatedAt();
    }
}
