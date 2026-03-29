package com.daker.domain.admin.dto;

import com.daker.domain.hackathon.domain.Hackathon;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class AdminHackathonUpdateResponse {

    private final Long id;
    private final LocalDateTime updatedAt;

    public AdminHackathonUpdateResponse(Hackathon hackathon) {
        this.id = hackathon.getId();
        this.updatedAt = hackathon.getUpdatedAt();
    }
}
