package com.redgit.auth.infrastructure.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final RedisService redisService;

    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final long BLOCK_DURATION_SECONDS = 300;
    private static final long ATTEMPT_WINDOW_SECONDS = 300;

    public boolean isBlocked(String email) {
        String blockKey = "blocked:" + email;
        boolean blocked = redisService.exists(blockKey);

        if (blocked) {
            long ttl = redisService.getTTL(blockKey);
            log.warn("Usuário bloqueado: {} (restam {}s)", email, ttl);
        }

        return blocked;
    }

    public int recordAttempt(String email) {
        String attemptKey = "ratelimit:" + email;
        Long attempts = redisService.incrementWithTTL(attemptKey, ATTEMPT_WINDOW_SECONDS);

        int currentAttempts = attempts != null ? attempts.intValue() : 0;
        log.debug("Tentativa de login registrada: {} (total: {})", email, currentAttempts);

        return currentAttempts;
    }

    public boolean checkAndBlock(String email) {
        int attempts = recordAttempt(email);

        if (attempts >= MAX_LOGIN_ATTEMPTS) {
            blockUser(email);
            log.warn("Usuário bloqueado por excesso de tentativas: {} ({} tentativas)",
                    email, attempts);
            return true;
        }

        return false;
    }

    public void blockUser(String email) {
        String blockKey = "blocked:" + email;
        redisService.set(blockKey, true, BLOCK_DURATION_SECONDS);
        log.info("Usuário bloqueado: {} por {}s", email, BLOCK_DURATION_SECONDS);
    }

    public void unblockUser(String email) {
        String blockKey = "blocked:" + email;
        String attemptKey = "ratelimit:" + email;

        redisService.delete(blockKey);
        redisService.delete(attemptKey);

        log.info("Usuário desbloqueado: {}", email);
    }

    public void resetAttempts(String email) {
        String attemptKey = "ratelimit:" + email;
        redisService.delete(attemptKey);
        log.debug("Tentativas resetadas para: {}", email);
    }

    public int getRemainingAttempts(String email) {
        String attemptKey = "ratelimit:" + email;
        Object value = redisService.get(attemptKey);

        if (value == null) {
            return MAX_LOGIN_ATTEMPTS;
        }

        int currentAttempts = value instanceof Number
                ? ((Number) value).intValue()
                : 0;

        return Math.max(0, MAX_LOGIN_ATTEMPTS - currentAttempts);
    }

    public long getBlockTimeRemaining(String email) {
        String blockKey = "blocked:" + email;
        long ttl = redisService.getTTL(blockKey);
        return ttl > 0 ? ttl : 0;
    }
}
