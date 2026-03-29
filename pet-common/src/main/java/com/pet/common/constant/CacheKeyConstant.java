package com.pet.common.constant;

import java.util.Arrays;
import java.util.List;

/**
 * 缓存常量类：统一管理缓存key前缀、过期时间、延迟删除时间
 * 规范：缓存key = 前缀 + 业务参数（比如CAT_DETAIL:1001）
 */
public class CacheKeyConstant {
    // ========== 缓存key前缀（项目名+业务，避免和其他系统冲突） ==========
    public static final String PET_RECOMMEND_PREFIX = "pet:recommend"; // 宠物推荐接口
    public static final String PET_LIST_PREFIX = "pet:list";           // 宠物查询接口
    public static final String PET_RECOMMEND_LOCK ="lock:pet:recommend";    // 分布式锁

    // ========== 基础过期时间（秒）==========
    public static final long CACHE_EXPIRE_BASE = 3600; // 基础过期时间1小时
    public static final long CACHE_EXPIRE_RANDOM = 1800; // 随机偏移量0.5小时（解决缓存雪崩）

    // ========== 延迟删除时间（毫秒，适配MySQL主从同步） ==========
    public static final long CACHE_DELAY_DELETE = 100; // 延迟100ms删除

    // ========== 布隆过滤器key（解决缓存穿透） ==========
    public static final String CAT_BLOOM_FILTER = "pet:id";

    // 热门猫咪ID（根据你的数据填，比如1,2,3）
    public static final List<Integer> HOT_PET_ID = Arrays.asList(1, 2, 3,4,7,29,33,41,47);

}