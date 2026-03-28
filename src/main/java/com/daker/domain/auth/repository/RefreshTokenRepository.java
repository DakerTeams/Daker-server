package com.daker.domain.auth.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;

/**
 * Redis 기반 RefreshToken 저장소.
 * JPA Repository가 아닌 StringRedisTemplate을 사용하여 Redis에 직접 접근한다.
 * key: "refresh_token:{userId}", value: refreshToken 문자열, TTL: jwt.refresh-expiration(초)
 * 로그아웃 또는 토큰 재발급 시 삭제하여 무효화한다.
 */
@Repository
@RequiredArgsConstructor
public class RefreshTokenRepository {

    private static final String KEY_PREFIX = "refresh_token:";

    private final StringRedisTemplate stringRedisTemplate;

    public void save(Long userId, String token, long ttlSeconds) {
        stringRedisTemplate.opsForValue().set(
                KEY_PREFIX + userId,
                token,
                Duration.ofSeconds(ttlSeconds)
        );
    }

    public Optional<String> findByUserId(Long userId) {
        return Optional.ofNullable(
                stringRedisTemplate.opsForValue().get(KEY_PREFIX + userId)
        );
    }

    public void deleteByUserId(Long userId) {
        stringRedisTemplate.delete(KEY_PREFIX + userId);
    }
}
