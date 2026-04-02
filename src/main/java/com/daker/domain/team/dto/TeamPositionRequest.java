package com.daker.domain.team.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class TeamPositionRequest {

    @NotBlank
    private String positionName;

    @Min(1)
    private Integer requiredCount;
}
