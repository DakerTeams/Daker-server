package com.daker.domain.auth.dto;

import com.daker.domain.user.domain.User;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class SignupResponse {

    private final Long userId;
    private final String nickname;
    private final String email;
    private final LocalDateTime createdAt;

    public SignupResponse(User user) {
        this.userId = user.getId();
        this.nickname = user.getNickname();
        this.email = user.getEmail();
        this.createdAt = user.getCreatedAt();
    }
}
