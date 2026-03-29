package com.daker.domain.admin.dto;

import com.daker.domain.hackathon.domain.Hackathon;
import com.daker.domain.hackathon.domain.HackathonStatus;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class AdminHackathonCreateResponse {

    private final Long id;
    private final String title;
    private final HackathonStatus status;
    private final LocalDateTime createdAt;

    public AdminHackathonCreateResponse(Hackathon hackathon) {
        this.id = hackathon.getId();
        this.title = hackathon.getTitle();
        this.status = hackathon.getStatus();
        this.createdAt = hackathon.getCreatedAt();
    }
}
