package com.daker.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class GithubLoginRequest {

    @NotBlank
    private String code;
}
