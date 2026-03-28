package com.daker.domain.team.dto;

import com.daker.domain.team.domain.ApplicationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class ApplicationDecisionRequest {

    @NotNull
    private ApplicationStatus status; // ACCEPTED or REJECTED
}
