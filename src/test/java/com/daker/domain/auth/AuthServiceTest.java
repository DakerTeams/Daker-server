package com.daker.domain.auth;

import com.daker.domain.auth.dto.*;
import com.daker.domain.auth.repository.RefreshTokenRepository;
import com.daker.domain.auth.service.AuthService;
import com.daker.domain.team.repository.TeamRepository;
import com.daker.domain.user.domain.AccountStatus;
import com.daker.domain.user.domain.Role;
import com.daker.domain.user.domain.User;
import com.daker.domain.user.repository.UserRepository;
import com.daker.global.auth.JwtProperties;
import com.daker.global.auth.JwtProvider;
import com.daker.global.exception.CustomException;
import com.daker.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock private UserRepository userRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private JwtProvider jwtProvider;
    @Mock private JwtProperties jwtProperties;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private TeamRepository teamRepository;

    private User mockUser() {
        return User.builder()
                .email("test@test.com")
                .nickname("tester")
                .password("encoded_pw")
                .role(Role.USER)
                .build();
    }

    @Test
    @DisplayName("회원가입 성공")
    void signup_success() {
        SignupRequest request = new SignupRequest();
        setField(request, "email", "new@test.com");
        setField(request, "nickname", "newuser");
        setField(request, "password", "password1!");

        given(userRepository.existsByEmail("new@test.com")).willReturn(false);
        given(userRepository.existsByNickname("newuser")).willReturn(false);
        given(passwordEncoder.encode(any())).willReturn("encoded_pw");
        given(userRepository.save(any())).willReturn(mockUser());

        SignupResponse response = authService.signup(request);

        assertThat(response).isNotNull();
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("이메일 중복 시 회원가입 실패")
    void signup_duplicateEmail() {
        SignupRequest request = new SignupRequest();
        setField(request, "email", "dup@test.com");
        setField(request, "nickname", "newuser");
        setField(request, "password", "password1!");

        given(userRepository.existsByEmail("dup@test.com")).willReturn(true);

        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.DUPLICATE_EMAIL);
    }

    @Test
    @DisplayName("닉네임 중복 시 회원가입 실패")
    void signup_duplicateNickname() {
        SignupRequest request = new SignupRequest();
        setField(request, "email", "new@test.com");
        setField(request, "nickname", "dup_nick");
        setField(request, "password", "password1!");

        given(userRepository.existsByEmail("new@test.com")).willReturn(false);
        given(userRepository.existsByNickname("dup_nick")).willReturn(true);

        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.DUPLICATE_NICKNAME);
    }

    @Test
    @DisplayName("로그인 성공")
    void login_success() {
        LoginRequest request = new LoginRequest();
        setField(request, "email", "test@test.com");
        setField(request, "password", "password1!");

        User user = mockUser();
        given(userRepository.findByEmailAndAccountStatus("test@test.com", AccountStatus.ACTIVE))
                .willReturn(Optional.of(user));
        given(passwordEncoder.matches(any(), any())).willReturn(true);
        given(jwtProvider.generateAccessToken(any(), any())).willReturn("access_token");
        given(jwtProvider.generateRefreshToken(any())).willReturn("refresh_token");
        given(jwtProperties.getAccessExpiration()).willReturn(3600L);
        given(jwtProperties.getRefreshExpiration()).willReturn(1209600L);

        LoginResponse response = authService.login(request);

        assertThat(response.getAccessToken()).isEqualTo("access_token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh_token");
    }

    @Test
    @DisplayName("비밀번호 불일치 시 로그인 실패")
    void login_wrongPassword() {
        LoginRequest request = new LoginRequest();
        setField(request, "email", "test@test.com");
        setField(request, "password", "wrong_pw");

        given(userRepository.findByEmailAndAccountStatus("test@test.com", AccountStatus.ACTIVE))
                .willReturn(Optional.of(mockUser()));
        given(passwordEncoder.matches(any(), any())).willReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_CREDENTIALS);
    }

    @Test
    @DisplayName("토큰 재발급 성공")
    void refresh_success() {
        TokenRefreshRequest request = new TokenRefreshRequest();
        setField(request, "refreshToken", "valid_refresh_token");

        given(jwtProvider.isValid("valid_refresh_token")).willReturn(true);
        given(jwtProvider.getUserId("valid_refresh_token")).willReturn(1L);
        given(refreshTokenRepository.findByUserId(1L)).willReturn(Optional.of("valid_refresh_token"));
        given(userRepository.findById(1L)).willReturn(Optional.of(mockUser()));
        given(jwtProvider.generateAccessToken(any(), any())).willReturn("new_access_token");
        given(jwtProperties.getAccessExpiration()).willReturn(3600L);

        TokenRefreshResponse response = authService.refresh(request);

        assertThat(response.getAccessToken()).isEqualTo("new_access_token");
    }

    @Test
    @DisplayName("유효하지 않은 리프레시 토큰으로 재발급 실패")
    void refresh_invalidToken() {
        TokenRefreshRequest request = new TokenRefreshRequest();
        setField(request, "refreshToken", "invalid_token");

        given(jwtProvider.isValid("invalid_token")).willReturn(false);

        assertThatThrownBy(() -> authService.refresh(request))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.REFRESH_TOKEN_EXPIRED);
    }

    @Test
    @DisplayName("로그아웃 시 Redis에서 토큰 삭제")
    void logout() {
        authService.logout(1L);

        verify(refreshTokenRepository).deleteByUserId(1L);
    }

    @Test
    @DisplayName("내 정보 조회 성공")
    void me_success() {
        given(userRepository.findById(1L)).willReturn(Optional.of(mockUser()));
        given(teamRepository.findAllByUserId(1L)).willReturn(java.util.List.of());

        MeResponse response = authService.me(1L);

        assertThat(response.getEmail()).isEqualTo("test@test.com");
        assertThat(response.getNickname()).isEqualTo("tester");
    }

    @Test
    @DisplayName("존재하지 않는 유저 조회 시 예외 발생")
    void me_userNotFound() {
        given(userRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> authService.me(99L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    // Lombok @Getter only DTO에 값 주입을 위한 리플렉션 헬퍼
    private void setField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
