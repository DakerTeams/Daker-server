package com.daker.domain.admin.dto;

import com.daker.domain.hackathon.domain.Hackathon;
import com.daker.domain.hackathon.domain.HackathonStatus;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class AdminHackathonUpdateResponse {

    private final Long id;
    private final HackathonStatus status;
    private final LocalDateTime updatedAt;

    public AdminHackathonUpdateResponse(Hackathon hackathon) {
        this.id = hackathon.getId();
        this.status = hackathon.getStatus();
        this.updatedAt = hackathon.getUpdatedAt();
    }
}
