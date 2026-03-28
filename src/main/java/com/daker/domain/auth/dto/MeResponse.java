package com.daker.domain.auth.dto;

import com.daker.domain.user.domain.AccountStatus;
import com.daker.domain.user.domain.Role;
import com.daker.domain.user.domain.User;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class MeResponse {

    private final Long userId;
    private final String nickname;
    private final String email;
    private final Role role;
    private final AccountStatus accountStatus;
    private final LocalDateTime createdAt;

    public MeResponse(User user) {
        this.userId = user.getId();
        this.nickname = user.getNickname();
        this.email = user.getEmail();
        this.role = user.getRole();
        this.accountStatus = user.getAccountStatus();
        this.createdAt = user.getCreatedAt();
    }
}
