package com.pet.common.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Redis分布式锁工具类（可重入、自动释放）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisDistributedLock {

    private final RedisTemplate<String, Object> redisTemplate;

    // 锁的前缀（避免key冲突）
    private static final String LOCK_PREFIX = "distributed:lock:";
    // 线程本地存储（存储当前线程的锁标识，实现可重入）
    private static final ThreadLocal<String> LOCK_IDENTIFIER = new ThreadLocal<>();

    /**
     * 获取分布式锁
     * @param lockKey 锁的key
     * @param expireTime 锁的过期时间（秒）
     * @param waitTime 等待获取锁的超时时间（秒）
     * @return 是否获取成功
     */
    public boolean lock(String lockKey, long expireTime, long waitTime) {
        // 生成唯一标识（防止误删其他线程的锁）
        String identifier = UUID.randomUUID().toString();
        LOCK_IDENTIFIER.set(identifier);
        String realKey = LOCK_PREFIX + lockKey;

        long startTime = System.currentTimeMillis();
        try {
            while (true) {
                // 1. 尝试获取锁（NX：不存在才设置，PX：毫秒过期）
                Boolean success = redisTemplate.opsForValue().setIfAbsent(
                        realKey, identifier, expireTime, TimeUnit.SECONDS
                );

                if (Boolean.TRUE.equals(success)) {
                    log.info("获取分布式锁成功，锁Key：{}，标识：{}", realKey, identifier);
                    return true;
                }

                // 2. 等待超时则返回失败
                if (System.currentTimeMillis() - startTime > waitTime * 1000) {
                    log.warn("获取分布式锁超时，锁Key：{}", realKey);
                    return false;
                }

                // 3. 未超时则短暂休眠后重试
                TimeUnit.MILLISECONDS.sleep(100);
            }
        } catch (InterruptedException e) {
            log.error("获取分布式锁被中断", e);
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * 释放分布式锁（防止误删）
     */
    public boolean unlock(String lockKey) {
        String realKey = LOCK_PREFIX + lockKey;
        String identifier = LOCK_IDENTIFIER.get();
        if (identifier == null) {
            log.warn("释放锁失败：当前线程无锁标识，锁Key：{}", realKey);
            return false;
        }

        // 使用Lua脚本保证原子性：先判断标识是否一致，一致才删除
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(script);
        redisScript.setResultType(Long.class);

        Long result = redisTemplate.execute(
                redisScript,
                Collections.singletonList(realKey),
                identifier
        );

        // 清理线程本地存储
        LOCK_IDENTIFIER.remove();

        if (result != null && result > 0) {
            log.info("释放分布式锁成功，锁Key：{}，标识：{}", realKey, identifier);
            return true;
        } else {
            log.warn("释放分布式锁失败（锁不存在或标识不匹配），锁Key：{}，标识：{}", realKey, identifier);
            return false;
        }
    }
}
