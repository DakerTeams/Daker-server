package com.daker.domain.team.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class TeamUpdateRequest {

    private String name;

    private String description;

    private Boolean isOpen;

    private Integer maxMemberCount;

    private List<TeamPositionRequest> positions;
}
