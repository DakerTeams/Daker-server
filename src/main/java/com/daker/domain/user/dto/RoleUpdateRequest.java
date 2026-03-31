package com.daker.domain.user.dto;

import com.daker.domain.user.domain.Role;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RoleUpdateRequest {

    private Role role;
}
