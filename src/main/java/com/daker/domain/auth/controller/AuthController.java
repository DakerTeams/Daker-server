package com.daker.domain.auth.controller;

import com.daker.domain.auth.dto.*;
import com.daker.domain.auth.service.AuthService;
import com.daker.domain.auth.service.GithubOAuthService;
import com.daker.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final GithubOAuthService githubOAuthService;

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<SignupResponse> signup(@Valid @RequestBody SignupRequest request) {
        return ApiResponse.created(authService.signup(request));
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ApiResponse<TokenRefreshResponse> refresh(@Valid @RequestBody TokenRefreshRequest request) {
        return ApiResponse.ok(authService.refresh(request));
    }

    @PostMapping("/logout")
    public ApiResponse<java.util.Map<String, String>> logout(@AuthenticationPrincipal Long userId) {
        authService.logout(userId);
        return ApiResponse.ok(java.util.Map.of("message", "로그아웃 되었습니다."));
    }

    @PostMapping("/github")
    public ApiResponse<LoginResponse> githubLogin(@Valid @RequestBody GithubLoginRequest request) {
        return ApiResponse.ok(githubOAuthService.login(request.getCode()));
    }

    @GetMapping("/me")
    public ApiResponse<MeResponse> me(@AuthenticationPrincipal Long userId) {
        return ApiResponse.ok(authService.me(userId));
    }
}
