package com.daker.domain.team.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.List;

@Getter
public class TeamCreateRequest {

    private Long hackathonId;

    @NotBlank
    private String name;

    private String description;

    @NotNull
    private Boolean isOpen;

    private Boolean isPublic;

    private Integer maxMemberCount;

    private List<TeamPositionRequest> positions;
}
