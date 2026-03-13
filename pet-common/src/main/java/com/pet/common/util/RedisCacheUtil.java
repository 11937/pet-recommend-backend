package com.pet.common.util;


import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 通用Redis缓存工具类（基于String类型，适配项目核心缓存需求）
 * 封装get/set/delete/延迟删除，统一缓存key规范、过期时间
 */
@Component
public class RedisCacheUtil {

    // 注入你已配置的RedisTemplate（和之前的Qualifier一致）
    @Resource
    @Qualifier("redisTemplate")
    private RedisTemplate<String, String> redisTemplate;

    // 延迟删除的线程池（单独创建，避免占用主线程）
    private static final ExecutorService CACHE_DELAY_POOL = Executors.newFixedThreadPool(5);

    // ========== 基础操作 ==========
    /**
     * 缓存存入（带过期时间）
     * @param key 缓存key（项目统一前缀+业务标识）
     * @param value 缓存值（建议转JSON字符串）
     * @param expire 过期时间（秒）
     */
    public void setCache(String key, String value, long expire) {
        ValueOperations<String, String> valueOps = redisTemplate.opsForValue();
        valueOps.set(key, value, expire, TimeUnit.SECONDS);
    }

    /**
     * 缓存获取
     * @param key 缓存key
     * @return 缓存值（null表示未命中）
     */
    public String getCache(String key) {
        if (key == null || "".equals(key)) {
            return null;
        }
        ValueOperations<String, String> valueOps = redisTemplate.opsForValue();
        return valueOps.get(key);
    }

    /**
     * 缓存删除
     * @param key 缓存key
     */
    public void deleteCache(String key) {
        if (key == null || "".equals(key)) {
            return;
        }
        redisTemplate.delete(key);
    }

    /**
     * 延迟删除缓存（解决MySQL主从同步、缓存更新脏数据问题）
     * @param key 缓存key
     * @param delay 延迟时间（毫秒，建议100-500ms）
     */
    public void delayDeleteCache(String key, long delay) {
        CACHE_DELAY_POOL.execute(() -> {
            try {
                Thread.sleep(delay);
                deleteCache(key);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                e.printStackTrace();
            }
        });
    }

    // ========== 参数MD5加密（解决多参数缓存key过长） ==========
    public static String md5(String str) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(str.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return str;
        }
    }

    // 关闭线程池（项目关闭时执行，避免内存泄漏）
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        CACHE_DELAY_POOL.shutdown();
    }
}