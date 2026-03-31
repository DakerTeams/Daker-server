package com.daker.domain.user.dto;

import com.daker.domain.user.domain.Role;
import com.daker.domain.user.domain.User;
import lombok.Getter;

@Getter
public class RoleUpdateResponse {

    private final Long id;
    private final String email;
    private final Role role;

    public RoleUpdateResponse(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.role = user.getRole();
    }
}
