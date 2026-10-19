package com.flashseat.flashseat_backend.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class RedisLockServiceTest {

    @Autowired RedisLockService redisLockService;

    @Test
    void testLockAcquisitionAndRelease() {
        String lockKey = "lock:test:seat:100";

        String token1 = redisLockService.acquireLock(lockKey, Duration.ofSeconds(5));
        assertNotNull(token1, "First lock attempt should succeed");
        String token2 = redisLockService.acquireLock(lockKey, Duration.ofSeconds(5));
        assertNull(token2, "Second attempt should fail");

        redisLockService.releaseLock(lockKey, token1);

        String token3 = redisLockService.acquireLock(lockKey, Duration.ofSeconds(5));
        assertNotNull(token3, "Lock acquisition should succeed after release");

        redisLockService.releaseLock(lockKey, token3);
    }
}
