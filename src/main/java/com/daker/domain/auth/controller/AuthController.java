package com.daker.domain.auth.controller;

import com.daker.domain.auth.dto.*;
import com.daker.domain.auth.service.AuthService;
import com.daker.domain.auth.service.GithubOAuthService;
import com.daker.global.response.ApiResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

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

    @GetMapping("/me")
    public ApiResponse<MeResponse> me(@AuthenticationPrincipal Long userId) {
        return ApiResponse.ok(authService.me(userId));
    }

    @GetMapping("/github/login")
    public ResponseEntity<Void> githubLogin() {
        try {
            GithubOAuthService.GithubAuthorization authorization = githubOAuthService.startAuthorization();

            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.SET_COOKIE, authorization.stateCookie().toString())
                    .location(URI.create(authorization.authorizationUrl()))
                    .build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.SET_COOKIE, githubOAuthService.buildExpiredStateCookie().toString())
                    .location(URI.create(githubOAuthService.buildFrontendErrorUrl(e.getMessage())))
                    .build();
        }
    }

    @GetMapping("/github/callback")
    public ResponseEntity<Void> githubCallback(@RequestParam(required = false) String code,
                                               @RequestParam(required = false) String state,
                                               @RequestParam(required = false) String error,
                                               HttpServletRequest request) {
        if (error != null && !error.isBlank()) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.SET_COOKIE, githubOAuthService.buildExpiredStateCookie().toString())
                    .location(URI.create(githubOAuthService.buildFrontendErrorUrl("GitHub 로그인이 취소되었습니다.")))
                    .build();
        }

        try {
            String savedState = readCookie(request, GithubOAuthService.STATE_COOKIE_NAME);
            GithubOAuthService.GithubCallbackResult result =
                    githubOAuthService.completeAuthorization(code, state, savedState);

            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.SET_COOKIE, result.expiredStateCookie().toString())
                    .location(URI.create(result.redirectUrl()))
                    .build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.SET_COOKIE, githubOAuthService.buildExpiredStateCookie().toString())
                    .location(URI.create(githubOAuthService.buildFrontendErrorUrl(e.getMessage())))
                    .build();
        }
    }

    private String readCookie(HttpServletRequest request, String cookieName) {
        if (request.getCookies() == null) {
            return null;
        }

        for (Cookie cookie : request.getCookies()) {
            if (cookieName.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        return null;
    }
}
