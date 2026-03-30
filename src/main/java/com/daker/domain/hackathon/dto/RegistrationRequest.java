package com.daker.domain.hackathon.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class RegistrationRequest {

    @NotNull
    private Long teamId;
}
