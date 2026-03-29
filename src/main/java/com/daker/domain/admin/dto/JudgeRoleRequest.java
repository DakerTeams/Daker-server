package com.daker.domain.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class JudgeRoleRequest {

    @NotBlank
    private String action; // "grant" | "revoke"
}
