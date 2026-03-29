package com.daker.domain.team.dto;

import lombok.Getter;

@Getter
public class TeamUpdateRequest {

    private String name;

    private String description;

    private Boolean isOpen;
}
