package com.daker.domain.user.controller;

import com.daker.domain.user.dto.RoleUpdateRequest;
import com.daker.domain.user.dto.RoleUpdateResponse;
import com.daker.domain.user.service.UserService;
import com.daker.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<RoleUpdateResponse> updateRole(
            @PathVariable Long id,
            @Valid @RequestBody RoleUpdateRequest request
    ) {
        return ApiResponse.ok(userService.updateRole(id, request));
    }
}
