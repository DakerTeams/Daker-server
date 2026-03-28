package com.daker.domain.user.dto;

import com.daker.domain.user.domain.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class RoleUpdateRequest {

    @NotNull
    private Role role;
}
