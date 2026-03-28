package com.daker.domain.auth.dto;

import com.daker.domain.user.domain.User;
import lombok.Getter;

@Getter
public class LoginResponse {

    private final String accessToken;
    private final String refreshToken;
    private final long expiresIn;
    private final UserInfo user;

    public LoginResponse(String accessToken, String refreshToken, long expiresIn, User user) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.user = new UserInfo(user);
    }

    @Getter
    public static class UserInfo {
        private final Long userId;
        private final String nickname;
        private final String email;

        public UserInfo(User user) {
            this.userId = user.getId();
            this.nickname = user.getNickname();
            this.email = user.getEmail();
        }
    }
}
