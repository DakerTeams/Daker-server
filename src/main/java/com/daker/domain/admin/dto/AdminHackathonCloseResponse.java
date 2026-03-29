package com.daker.domain.admin.dto;

import com.daker.domain.hackathon.domain.Hackathon;
import com.daker.domain.hackathon.domain.HackathonStatus;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class AdminHackathonCloseResponse {

    private final Long id;
    private final HackathonStatus status;
    private final LocalDateTime closedAt;

    public AdminHackathonCloseResponse(Hackathon hackathon) {
        this.id = hackathon.getId();
        this.status = hackathon.getStatus();
        this.closedAt = hackathon.getClosedAt();
    }
}
