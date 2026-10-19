package com.flashseat.flashseat_backend.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;

@Service
public class RedisLockService {

    private final StringRedisTemplate redisTemplate;

    public RedisLockService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public String acquireLock(String lockKey, Duration timeoutDuration) {
        String lockValue = UUID.randomUUID().toString();

        Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, lockValue, timeoutDuration);
        return Boolean.TRUE.equals(success) ? lockValue : null;
    }

    public Map<String, String> acquireLocks(List<String> lockKeys, Duration timeoutDuration) {
        Map<String, String> acquiredLocks = new HashMap<>();

        for (String lockKey : lockKeys) {
            String token = acquireLock(lockKey, timeoutDuration);
            if (token == null) {
                for (Map.Entry<String, String> entry : acquiredLocks.entrySet()) {
                    releaseLock(entry.getKey(), entry.getValue());
                }
                return null;
            }
            acquiredLocks.put(lockKey, token);
        }
        return acquiredLocks;
    }
    public void releaseLock(String lockKey, String lockValue) {
        String currentValue = redisTemplate.opsForValue().get(lockKey);
        if(Objects.equals(currentValue, lockValue)) {
            redisTemplate.delete(lockKey);
        }
    }
}
