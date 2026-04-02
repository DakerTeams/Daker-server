package com.daker.domain.team.dto;

import com.daker.domain.team.domain.ApplicationStatus;
import com.daker.domain.team.domain.TeamApplication;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class TeamApplicationResponse {

    private final Long applicationId;
    private final Long userId;
    private final String nickname;
    private final String position;
    private final ApplicationStatus status;
    private final LocalDateTime createdAt;

    public TeamApplicationResponse(TeamApplication application) {
        this.applicationId = application.getId();
        this.userId = application.getUser().getId();
        this.nickname = application.getUser().getNickname();
        this.position = application.getPosition();
        this.status = application.getStatus();
        this.createdAt = application.getCreatedAt();
    }
}
