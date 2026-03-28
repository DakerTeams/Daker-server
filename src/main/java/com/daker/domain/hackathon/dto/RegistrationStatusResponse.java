package com.daker.domain.hackathon.dto;

import com.daker.domain.hackathon.domain.HackathonRegistration;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class RegistrationStatusResponse {

    private final Long hackathonId;
    private final Long teamId;
    private final String teamName;
    private final LocalDateTime registeredAt;

    public RegistrationStatusResponse(HackathonRegistration registration) {
        this.hackathonId = registration.getHackathon().getId();
        this.teamId = registration.getTeam().getId();
        this.teamName = registration.getTeam().getName();
        this.registeredAt = registration.getRegisteredAt();
    }
}
