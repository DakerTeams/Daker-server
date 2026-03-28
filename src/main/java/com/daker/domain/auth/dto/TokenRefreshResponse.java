package com.daker.domain.auth.dto;

import lombok.Getter;

@Getter
public class TokenRefreshResponse {

    private final String accessToken;
    private final long expiresIn;

    public TokenRefreshResponse(String accessToken, long expiresIn) {
        this.accessToken = accessToken;
        this.expiresIn = expiresIn;
    }
}
