package com.daker.domain.team.domain;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum ApplicationStatus {
    PENDING, ACCEPTED, REJECTED;

    @JsonCreator
    public static ApplicationStatus from(String value) {
        return ApplicationStatus.valueOf(value.toUpperCase());
    }
}
