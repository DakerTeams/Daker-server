package com.daker.domain.auth.service;

import com.daker.domain.auth.dto.LoginResponse;
import com.daker.domain.auth.repository.RefreshTokenRepository;
import com.daker.domain.user.domain.Role;
import com.daker.domain.user.domain.User;
import com.daker.domain.user.repository.UserRepository;
import com.daker.global.auth.GithubProperties;
import com.daker.global.auth.JwtProperties;
import com.daker.global.auth.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GithubOAuthService {

    private static final String GITHUB_TOKEN_URL = "https://github.com/login/oauth/access_token";
    private static final String GITHUB_USER_URL = "https://api.github.com/user";
    private static final String GITHUB_EMAIL_URL = "https://api.github.com/user/emails";

    private final GithubProperties githubProperties;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProvider jwtProvider;
    private final JwtProperties jwtProperties;
    private final RestTemplate restTemplate = new RestTemplate();

    @Transactional
    public LoginResponse login(String code) {
        String accessToken = exchangeCodeForToken(code);
        Map<String, Object> userInfo = fetchGithubUser(accessToken);

        String email = resolveEmail(userInfo, accessToken);
        String githubLogin = (String) userInfo.get("login");

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> createUser(email, githubLogin));

        String jwtAccess = jwtProvider.generateAccessToken(user.getId(), user.getRole().name());
        String jwtRefresh = jwtProvider.generateRefreshToken(user.getId());
        refreshTokenRepository.save(user.getId(), jwtRefresh, jwtProperties.getRefreshExpiration());

        return new LoginResponse(jwtAccess, jwtRefresh, jwtProperties.getAccessExpiration(), user);
    }

    private String exchangeCodeForToken(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        Map<String, String> body = Map.of(
                "client_id", githubProperties.getClientId(),
                "client_secret", githubProperties.getClientSecret(),
                "code", code,
                "redirect_uri", githubProperties.getRedirectUri()
        );

        @SuppressWarnings("unchecked")
        Map<String, Object> response = restTemplate.exchange(
                GITHUB_TOKEN_URL,
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                Map.class
        ).getBody();

        if (response == null || !response.containsKey("access_token")) {
            throw new IllegalStateException("GitHub access token 발급 실패");
        }

        return (String) response.get("access_token");
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> fetchGithubUser(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.set("X-GitHub-Api-Version", "2022-11-28");

        return restTemplate.exchange(
                GITHUB_USER_URL,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class
        ).getBody();
    }

    private String resolveEmail(Map<String, Object> userInfo, String accessToken) {
        String email = (String) userInfo.get("email");
        if (email != null) {
            return email;
        }

        // 이메일이 비공개인 경우 emails API 호출
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.set("X-GitHub-Api-Version", "2022-11-28");

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> emails = restTemplate.exchange(
                GITHUB_EMAIL_URL,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                List.class
        ).getBody();

        if (emails == null || emails.isEmpty()) {
            throw new IllegalStateException("GitHub 이메일 정보를 가져올 수 없습니다.");
        }

        return emails.stream()
                .filter(e -> Boolean.TRUE.equals(e.get("primary")))
                .map(e -> (String) e.get("email"))
                .findFirst()
                .orElse((String) emails.get(0).get("email"));
    }

    private User createUser(String email, String githubLogin) {
        String nickname = resolveUniqueNickname(githubLogin);
        return userRepository.save(User.builder()
                .email(email)
                .nickname(nickname)
                .password(null)
                .role(Role.USER)
                .build());
    }

    private String resolveUniqueNickname(String base) {
        if (!userRepository.existsByNickname(base)) {
            return base;
        }
        int suffix = 1;
        while (userRepository.existsByNickname(base + suffix)) {
            suffix++;
        }
        return base + suffix;
    }
}
