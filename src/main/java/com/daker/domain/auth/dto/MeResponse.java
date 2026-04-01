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
    private final int points;
    private final int rank;
    private final int joinedHackathons;
    private final LocalDateTime createdAt;

    public MeResponse(User user, int joinedHackathons, int points, int rank) {
        this.userId = user.getId();
        this.nickname = user.getNickname();
        this.email = user.getEmail();
        this.role = user.getRole();
        this.accountStatus = user.getAccountStatus();
        this.points = points;
        this.rank = rank;
        this.joinedHackathons = joinedHackathons;
        this.createdAt = user.getCreatedAt();
    }
}
