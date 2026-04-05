package com.daker.domain.auth.service;

import com.daker.domain.auth.dto.*;
import com.daker.domain.auth.repository.RefreshTokenRepository;
import com.daker.domain.ranking.dto.MyRankingResponse;
import com.daker.domain.ranking.dto.RankingPeriod;
import com.daker.domain.ranking.service.RankingService;
import com.daker.domain.team.repository.TeamRepository;
import com.daker.domain.user.domain.AccountStatus;
import com.daker.domain.user.domain.Role;
import com.daker.domain.user.domain.User;
import com.daker.domain.user.repository.UserRepository;
import com.daker.domain.user.repository.UserTagRepository;
import com.daker.global.auth.JwtProperties;
import com.daker.global.auth.JwtProvider;
import com.daker.global.exception.CustomException;
import com.daker.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TeamRepository teamRepository;
    private final UserTagRepository userTagRepository;
    private final RankingService rankingService;
    private final JwtProvider jwtProvider;
    private final JwtProperties jwtProperties;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public SignupResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        }
        if (userRepository.existsByNickname(request.getNickname())) {
            throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
        }

        User user = User.builder()
                .email(request.getEmail())
                .nickname(request.getNickname())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();

        return new SignupResponse(userRepository.save(user));
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmailAndAccountStatus(request.getEmail(), AccountStatus.ACTIVE)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_CREDENTIALS));

        if (user.getPassword() == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_CREDENTIALS);
        }

        return issueLogin(user);
    }

    @Transactional
    public LoginResponse issueLogin(User user) {
        String accessToken = jwtProvider.generateAccessToken(user.getId(), user.getRole().name());
        String refreshToken = jwtProvider.generateRefreshToken(user.getId());
        refreshTokenRepository.save(user.getId(), refreshToken, jwtProperties.getRefreshExpiration());

        return new LoginResponse(accessToken, refreshToken, jwtProperties.getAccessExpiration(), user);
    }

    @Transactional
    public TokenRefreshResponse refresh(TokenRefreshRequest request) {
        String token = request.getRefreshToken();

        if (!jwtProvider.isValid(token)) {
            throw new CustomException(ErrorCode.REFRESH_TOKEN_EXPIRED);
        }

        Long userId = jwtProvider.getUserId(token);

        refreshTokenRepository.findByUserId(userId)
                .filter(saved -> saved.equals(token))
                .orElseThrow(() -> new CustomException(ErrorCode.REFRESH_TOKEN_EXPIRED));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        String newAccessToken = jwtProvider.generateAccessToken(user.getId(), user.getRole().name());

        return new TokenRefreshResponse(newAccessToken, jwtProperties.getAccessExpiration());
    }

    @Transactional
    public void logout(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }

    @Transactional(readOnly = true)
    public MeResponse me(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        int joinedHackathons = (int) teamRepository.findAllByUserId(userId).stream()
                .filter(t -> t.getHackathon() != null)
                .count();

        MyRankingResponse myRanking = rankingService.getMyRanking(RankingPeriod.ALL, userId);

        return new MeResponse(user, joinedHackathons, myRanking.getScoreRank().getPoints(), myRanking.getScoreRank().getRank(),
                userTagRepository.findAllByUserId(userId));
    }
}
