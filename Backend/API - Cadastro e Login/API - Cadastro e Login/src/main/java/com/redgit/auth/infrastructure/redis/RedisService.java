package com.redgit.auth.infrastructure.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String KEY_PREFIX = "auth:";

    public void set(String key, Object value, long ttlSeconds) {
        try {
            String fullKey = KEY_PREFIX + key;
            redisTemplate.opsForValue().set(fullKey, value, ttlSeconds, TimeUnit.SECONDS);
            log.debug("Redis SET: {} (TTL: {}s)", fullKey, ttlSeconds);
        } catch (Exception e) {
            log.error("Erro ao salvar no Redis: key={}, error={}", key, e.getMessage());
        }
    }

    public void set(String key, Object value) {
        try {
            String fullKey = KEY_PREFIX + key;
            redisTemplate.opsForValue().set(fullKey, value);
            log.debug("Redis SET (sem TTL): {}", fullKey);
        } catch (Exception e) {
            log.error("Erro ao salvar no Redis: key={}, error={}", key, e.getMessage());
        }
    }

    public Object get(String key) {
        try {
            String fullKey = KEY_PREFIX + key;
            Object value = redisTemplate.opsForValue().get(fullKey);
            log.debug("Redis GET: {} = {}", fullKey, value != null ? "HIT" : "MISS");
            return value;
        } catch (Exception e) {
            log.error("Erro ao buscar no Redis: key={}, error={}", key, e.getMessage());
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> type) {
        Object value = get(key);
        if (value != null && type.isInstance(value)) {
            return (T) value;
        }
        return null;
    }

    public boolean delete(String key) {
        try {
            String fullKey = KEY_PREFIX + key;
            Boolean deleted = redisTemplate.delete(fullKey);
            log.debug("Redis DELETE: {} = {}", fullKey, deleted);
            return Boolean.TRUE.equals(deleted);
        } catch (Exception e) {
            log.error("Erro ao deletar do Redis: key={}, error={}", key, e.getMessage());
            return false;
        }
    }

    public boolean exists(String key) {
        try {
            String fullKey = KEY_PREFIX + key;
            Boolean exists = redisTemplate.hasKey(fullKey);
            return Boolean.TRUE.equals(exists);
        } catch (Exception e) {
            log.error("Erro ao verificar existência no Redis: key={}, error={}", key, e.getMessage());
            return false;
        }
    }

    public boolean expire(String key, long ttlSeconds) {
        try {
            String fullKey = KEY_PREFIX + key;
            Boolean result = redisTemplate.expire(fullKey, ttlSeconds, TimeUnit.SECONDS);
            log.debug("Redis EXPIRE: {} = {}s", fullKey, ttlSeconds);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.error("Erro ao definir expiração no Redis: key={}, error={}", key, e.getMessage());
            return false;
        }
    }

    public long getTTL(String key) {
        try {
            String fullKey = KEY_PREFIX + key;
            Long ttl = redisTemplate.getExpire(fullKey, TimeUnit.SECONDS);
            return ttl != null ? ttl : -2;
        } catch (Exception e) {
            log.error("Erro ao obter TTL do Redis: key={}, error={}", key, e.getMessage());
            return -2;
        }
    }

    public Long increment(String key) {
        try {
            String fullKey = KEY_PREFIX + key;
            Long value = redisTemplate.opsForValue().increment(fullKey);
            log.debug("Redis INCREMENT: {} = {}", fullKey, value);
            return value;
        } catch (Exception e) {
            log.error("Erro ao incrementar no Redis: key={}, error={}", key, e.getMessage());
            return null;
        }
    }

    public Long incrementWithTTL(String key, long ttlSeconds) {
        try {
            String fullKey = KEY_PREFIX + key;
            Long value = redisTemplate.opsForValue().increment(fullKey);

            if (value != null && value == 1) {
                redisTemplate.expire(fullKey, ttlSeconds, TimeUnit.SECONDS);
            }

            log.debug("Redis INCREMENT (TTL: {}s): {} = {}", ttlSeconds, fullKey, value);
            return value;
        } catch (Exception e) {
            log.error("Erro ao incrementar com TTL no Redis: key={}, error={}", key, e.getMessage());
            return null;
        }
    }
}
