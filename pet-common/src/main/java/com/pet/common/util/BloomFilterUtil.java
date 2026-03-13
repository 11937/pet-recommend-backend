package com.pet.common.util;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import java.util.List;
import javax.annotation.Resource;


@Component
public class BloomFilterUtil {
    @Resource
    private RedisTemplate<String, String> redisTemplate;

    // 根据预估数据量计算得出
    private static final int BIT_SIZE = 10000;   // 位数组长度
    private static final int HASH_NUM = 7;       // 哈希函数个数

    // 添加元素
    public void add(String key, String value) {
        int[] offsets = getOffsets(value);
        for (int offset : offsets) {
            redisTemplate.opsForValue().setBit(key, offset, true);
        }
    }

    // 判断元素是否存在
    public boolean contains(String key, String value) {
        int[] offsets = getOffsets(value);
        for (int offset : offsets) {
            Boolean bit = redisTemplate.opsForValue().getBit(key, offset);
            if (bit == null || !bit) {
                return false;
            }
        }
        return true;
    }

    // 计算多个偏移量（保证在[0, BIT_SIZE)内）
    private int[] getOffsets(String value) {
        int[] offsets = new int[HASH_NUM];
        // 使用两个基础哈希
        int hash1 = Math.abs(value.hashCode());
        int hash2 = Math.abs((value + "bloom").hashCode());
        for (int i = 0; i < HASH_NUM; i++) {
            long combined = (hash1 + (long) i * hash2) % BIT_SIZE;
            offsets[i] = (int) combined;
        }
        return offsets;
    }

    // 初始化布隆过滤器（可选）
    public void init(String key, List<String> values) {
        // 可选：预分配位数组
        redisTemplate.opsForValue().setBit(key, BIT_SIZE - 1, false);
        for (String v : values) {
            add(key, v);
        }
    }
}
