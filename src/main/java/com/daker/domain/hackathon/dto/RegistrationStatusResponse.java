package com.daker.domain.hackathon.dto;

import com.daker.domain.hackathon.domain.HackathonRegistration;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class RegistrationStatusResponse {

    private final boolean registered;
    private final Long registrationId;
    private final Long teamId;
    private final String teamName;
    private final LocalDateTime registeredAt;

    public RegistrationStatusResponse(HackathonRegistration registration) {
        this.registered = true;
        this.registrationId = registration.getId();
        this.teamId = registration.getTeam().getId();
        this.teamName = registration.getTeam().getName();
        this.registeredAt = registration.getRegisteredAt();
    }

    public static RegistrationStatusResponse notRegistered() {
        return new RegistrationStatusResponse();
    }

    private RegistrationStatusResponse() {
        this.registered = false;
        this.registrationId = null;
        this.teamId = null;
        this.teamName = null;
        this.registeredAt = null;
    }
}
