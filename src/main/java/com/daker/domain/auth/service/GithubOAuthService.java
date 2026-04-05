package com.daker.domain.auth.service;

import com.daker.domain.auth.dto.LoginResponse;
import com.daker.domain.user.domain.Role;
import com.daker.domain.user.domain.User;
import com.daker.domain.user.repository.UserRepository;
import com.daker.global.exception.CustomException;
import com.daker.global.exception.ErrorCode;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GithubOAuthService {

    public static final String STATE_COOKIE_NAME = "github_oauth_state";

    private final UserRepository userRepository;
    private final AuthService authService;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Value("${github.oauth.client-id:}")
    private String clientId;

    @Value("${github.oauth.client-secret:}")
    private String clientSecret;

    @Value("${github.oauth.redirect-uri}")
    private String redirectUri;

    public GithubAuthorization startAuthorization() {
        validateGithubConfig();

        String state = generateState();
        String authorizationUrl = "https://github.com/login/oauth/authorize"
                + "?client_id=" + encode(clientId)
                + "&redirect_uri=" + encode(redirectUri)
                + "&scope=" + encode("read:user user:email")
                + "&state=" + encode(state);

        return new GithubAuthorization(
                authorizationUrl,
                buildStateCookie(state),
                buildStateCookie("")
        );
    }

    public GithubCallbackResult completeAuthorization(String code, String state, String savedState) {
        validateGithubConfig();

        if (savedState == null || savedState.isBlank() || !savedState.equals(state)) {
            throw new CustomException(ErrorCode.GITHUB_STATE_INVALID);
        }

        GithubTokenResponse tokenResponse = exchangeCodeForToken(code, state);
        GithubUserProfile profile = fetchGithubUser(tokenResponse.accessToken());
        String email = resolveGithubEmail(tokenResponse.accessToken(), profile);

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> createGithubUser(email, profile));

        LoginResponse loginResponse = authService.issueLogin(user);

        return new GithubCallbackResult(
                buildFrontendSuccessUrl(loginResponse),
                buildStateCookie("")
        );
    }

    public String buildFrontendErrorUrl(String errorMessage) {
        return frontendUrl + "/auth/github/callback?error=" + encode(errorMessage);
    }

    public ResponseCookie buildExpiredStateCookie() {
        return buildStateCookie("");
    }

    private void validateGithubConfig() {
        if (clientId == null || clientId.isBlank() || clientSecret == null || clientSecret.isBlank()) {
            throw new CustomException(ErrorCode.GITHUB_AUTH_FAILED);
        }
    }

    private String buildFrontendSuccessUrl(LoginResponse loginResponse) {
        return frontendUrl + "/auth/github/callback"
                + "#accessToken=" + encode(loginResponse.getAccessToken())
                + "&refreshToken=" + encode(loginResponse.getRefreshToken())
                + "&expiresIn=" + loginResponse.getExpiresIn();
    }

    private User createGithubUser(String email, GithubUserProfile profile) {
        String nickname = buildUniqueNickname(profile);

        User user = User.builder()
                .email(email)
                .nickname(nickname)
                .password(null)
                .role(Role.USER)
                .build();

        return userRepository.save(user);
    }

    private String buildUniqueNickname(GithubUserProfile profile) {
        String base = firstNonBlank(profile.login(), profile.name(), emailPrefix(profile.email()), "github-user");
        String normalized = base.replaceAll("[^A-Za-z0-9_\\-]", "");

        if (normalized.isBlank()) {
            normalized = "github-user";
        }

        if (normalized.length() > 20) {
            normalized = normalized.substring(0, 20);
        }

        String candidate = normalized;
        int suffix = 1;

        while (userRepository.existsByNickname(candidate)) {
            String suffixText = String.valueOf(suffix++);
            int maxBaseLength = Math.max(1, 20 - suffixText.length());
            String prefix = normalized.length() > maxBaseLength ? normalized.substring(0, maxBaseLength) : normalized;
            candidate = prefix + suffixText;
        }

        return candidate;
    }

    private GithubTokenResponse exchangeCodeForToken(String code, String state) {
        String form = "client_id=" + encode(clientId)
                + "&client_secret=" + encode(clientSecret)
                + "&code=" + encode(code)
                + "&redirect_uri=" + encode(redirectUri)
                + "&state=" + encode(state);

        HttpRequest request = HttpRequest.newBuilder(URI.create("https://github.com/login/oauth/access_token"))
                .header(HttpHeaders.ACCEPT, "application/json")
                .header(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                throw new CustomException(ErrorCode.GITHUB_AUTH_FAILED);
            }

            GithubTokenResponse body = objectMapper.readValue(response.body(), GithubTokenResponse.class);
            if (body.accessToken() == null || body.accessToken().isBlank()) {
                throw new CustomException(ErrorCode.GITHUB_AUTH_FAILED);
            }

            return body;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CustomException(ErrorCode.GITHUB_AUTH_FAILED);
        } catch (IOException e) {
            throw new CustomException(ErrorCode.GITHUB_AUTH_FAILED);
        }
    }

    private GithubUserProfile fetchGithubUser(String accessToken) {
        HttpRequest request = HttpRequest.newBuilder(URI.create("https://api.github.com/user"))
                .header(HttpHeaders.ACCEPT, "application/vnd.github+json")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                throw new CustomException(ErrorCode.GITHUB_AUTH_FAILED);
            }

            return objectMapper.readValue(response.body(), GithubUserProfile.class);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CustomException(ErrorCode.GITHUB_AUTH_FAILED);
        } catch (IOException e) {
            throw new CustomException(ErrorCode.GITHUB_AUTH_FAILED);
        }
    }

    private String resolveGithubEmail(String accessToken, GithubUserProfile profile) {
        if (profile.email() != null && !profile.email().isBlank()) {
            return profile.email();
        }

        HttpRequest request = HttpRequest.newBuilder(URI.create("https://api.github.com/user/emails"))
                .header(HttpHeaders.ACCEPT, "application/vnd.github+json")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                throw new CustomException(ErrorCode.GITHUB_EMAIL_NOT_FOUND);
            }

            List<GithubEmail> emails = List.of(objectMapper.readValue(response.body(), GithubEmail[].class));
            return emails.stream()
                    .filter(email -> email.email() != null && !email.email().isBlank())
                    .sorted(Comparator
                            .comparing(GithubEmail::primary).reversed()
                            .thenComparing(GithubEmail::verified).reversed())
                    .map(GithubEmail::email)
                    .findFirst()
                    .orElseThrow(() -> new CustomException(ErrorCode.GITHUB_EMAIL_NOT_FOUND));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CustomException(ErrorCode.GITHUB_EMAIL_NOT_FOUND);
        } catch (IOException e) {
            throw new CustomException(ErrorCode.GITHUB_EMAIL_NOT_FOUND);
        }
    }

    private ResponseCookie buildStateCookie(String value) {
        return ResponseCookie.from(STATE_COOKIE_NAME, value)
                .httpOnly(true)
                .secure(frontendUrl.startsWith("https://"))
                .sameSite("Lax")
                .path("/")
                .maxAge(value.isBlank() ? 0 : 300)
                .build();
    }

    private String generateState() {
        byte[] bytes = new byte[16];
        secureRandom.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private String emailPrefix(String email) {
        if (email == null || !email.contains("@")) {
            return null;
        }
        return email.substring(0, email.indexOf('@'));
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    public record GithubAuthorization(String authorizationUrl,
                                      ResponseCookie stateCookie,
                                      ResponseCookie expiredStateCookie) {
    }

    public record GithubCallbackResult(String redirectUrl, ResponseCookie expiredStateCookie) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GithubTokenResponse(@JsonProperty("access_token") String accessToken) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GithubUserProfile(String login, String name, String email) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GithubEmail(String email, boolean primary, boolean verified) {
    }
}
