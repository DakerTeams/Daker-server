package com.daker.domain.team.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class TeamCreateRequest {

    @NotNull
    private Long hackathonId;

    @NotBlank
    private String name;

    private String description;

    @NotNull
    private Boolean isOpen;
}
