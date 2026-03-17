package com.pet.cat.recommend.service.impl;


import com.alibaba.fastjson2.JSON;
import com.pet.cat.recommend.feign.CatInfoFeignClient;
import com.pet.cat.recommend.service.CatRecommendService;
import com.pet.common.constant.CacheKeyConstant;
import com.pet.common.dto.Cat;
import com.pet.common.dto.CatRecommendDTO;
import com.pet.common.entity.Result;
import com.pet.common.util.RedisCacheUtil;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;


@Service
@Slf4j // 替换 System.out
public class CatRecommendServiceImpl implements CatRecommendService {

    @Resource
    private CatInfoFeignClient catInfoFeignClient;

    @Resource
    private RedisCacheUtil redisCacheUtil;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /** 远程调用次数统计 */
    private final AtomicInteger remoteCallCount = new AtomicInteger(0);

    /** 随机数（防雪崩） */
    private static final Random RANDOM = new Random();

    // ===================== 推荐权重常量 =====================
    private static final int CARE_HOURS_WEIGHT = 40;
    private static final int HOUSE_TYPE_WEIGHT = 20;
    private static final int EXPERIENCE_WEIGHT = 50;
    private static final int BUDGET_WEIGHT = 30;
    private static final int SHEDDING_WEIGHT = 40;
    private static final int PERSONALITY_WEIGHT = 10;

    /** 空值缓存标记 */
    private static final List<Cat> EMPTY_CAT_LIST = new ArrayList<>(0);

    @Override
    public List<Cat> recommendCats(CatRecommendDTO dto) {
        String cacheKey = CacheKeyConstant.CAT_RECOMMEND_PREFIX;
        log.info("【Redis】生成缓存 key：{}", cacheKey);

        // 1. 查询缓存
        List<Cat> catList = getCache(cacheKey);
        if (catList != null) {
            // 缓存是空值标记，直接返回空
            if (catList.isEmpty()) {
                log.info("【Redis】缓存空值命中，直接返回空列表");
                return new ArrayList<>();
            }
            log.info("【Redis】全量猫咪缓存命中，数量：{}", catList.size());
        }

        // 2. 缓存未命中 → 加锁查询远程（防止缓存击穿）
        if (catList == null) {
            catList = loadDataFromRemoteWithLock(cacheKey);
        }

        // 3. 无数据直接返回
        if (catList.isEmpty()) {
            return new ArrayList<>();
        }

        // 4. 推荐算法：打分 + 排序 + 取前10
        return catList.stream()
                .peek(cat -> cat.setScore(calculateScore(cat, dto)))
                .sorted((c1, c2) -> Integer.compare(c2.getScore(), c1.getScore()))
                .limit(10)
                .collect(Collectors.toList());
    }

    // ===================== 缓存获取（含空值解析） =====================
    private List<Cat> getCache(String cacheKey) {
        try {
            String cacheValue = redisCacheUtil.getCache(cacheKey);
            if (cacheValue == null || cacheValue.isEmpty()) {
                return null;
            }
            return JSON.parseArray(cacheValue, Cat.class);
        } catch (Exception e) {
            log.error("Redis 缓存查询异常", e);
            return null;
        }
    }

    // ===================== 带分布式锁的远程加载（防击穿） =====================
    private List<Cat> loadDataFromRemoteWithLock(String cacheKey) {
        // 锁 key
        String lockKey = CacheKeyConstant.CAT_RECOMMEND_LOCK;
        try {
            // 尝试加锁（5秒自动释放）
            Boolean lock = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", 5, java.util.concurrent.TimeUnit.SECONDS);
            if (Boolean.FALSE.equals(lock)) {
                // 未获取到锁，直接返回空（或重试）
                log.warn("未获取到分布式锁，直接返回空");
                return new ArrayList<>();
            }

            // 再次检查缓存（双重检查）
            List<Cat> cacheCatList = getCache(cacheKey);
            if (cacheCatList != null) {
                return cacheCatList.isEmpty() ? new ArrayList<>() : cacheCatList;
            }

            // 真正执行远程调用
            return loadDataFromRemote(cacheKey);
        } finally {
            // 释放锁
            redisTemplate.delete(lockKey);
        }
    }

    // ===================== 真正远程调用 + 写入缓存 =====================
    private List<Cat> loadDataFromRemote(String cacheKey) {
        log.info("【远程调用】开始调用 cat-info 服务，累计调用次数：{}", remoteCallCount.incrementAndGet());

        try {
            // 远程调用
            Result<List<Cat>> result = catInfoFeignClient.findAllCatList();
            if (result == null || result.getCode() != 200 || result.getData() == null) {
                log.error("【远程调用】服务异常或无数据");
                // 缓存空值（防穿透）
                setCacheWithNull(cacheKey);
                return new ArrayList<>();
            }

            List<Cat> catList = result.getData();
            if (catList.isEmpty()) {
                setCacheWithNull(cacheKey);
                return new ArrayList<>();
            }

            // 写入缓存（随机过期时间防雪崩）
            long expire = CacheKeyConstant.CACHE_EXPIRE_BASE
                    + RANDOM.nextInt((int) CacheKeyConstant.CACHE_EXPIRE_RANDOM);

            redisCacheUtil.setCache(cacheKey, JSON.toJSONString(catList), expire);
            log.info("【Redis】✅ 全量猫咪写入缓存，数量：{}，过期时间：{}秒", catList.size(), expire);

            return catList;
        } catch (Exception e) {
            log.error("【远程调用】失败", e);
            setCacheWithNull(cacheKey);
            return new ArrayList<>();
        }
    }

    // ===================== 缓存空值（防穿透） =====================
    private void setCacheWithNull(String cacheKey) {
        try {
            // 空值缓存时间短一点
            long expire = 60 + RANDOM.nextInt(60);
            redisCacheUtil.setCache(cacheKey, JSON.toJSONString(EMPTY_CAT_LIST), expire);
            log.info("【Redis】空值已缓存，防止缓存穿透");
        } catch (Exception e) {
            log.error("空值缓存失败", e);
        }
    }

    // ===================== 推荐打分算法 =====================
    private int calculateScore(Cat cat, CatRecommendDTO dto) {
        int score = 0;

        // 居住环境
        if (dto.getSuitableHousing() != null && dto.getSuitableHousing().equals(cat.getSuitableHousing())) {
            score += HOUSE_TYPE_WEIGHT;
        }

        // 养宠经验
        if (dto.getSuitableExperience() != null && dto.getSuitableExperience().equals(cat.getSuitableExperience())) {
            score += EXPERIENCE_WEIGHT;
        }

        // 预算
        if (dto.getBudgetLevel() != null && dto.getBudgetLevel().equals(cat.getBudgetLevel())) {
            score += BUDGET_WEIGHT;
        }

        // 性格（多选用逗号分隔）
        score += calculateMatchScore(
                dto.getPersonalityType(),
                cat.getPersonalityType(),
                PERSONALITY_WEIGHT
        );

        // 掉毛程度
        score += calculateMatchScore(
                dto.getSheddingDegree(),
                cat.getSheddingDegree(),
                SHEDDING_WEIGHT
        );

        // 陪伴时间
        if (dto.getRequiredCareHours() != null && dto.getRequiredCareHours().equals(cat.getRequiredCareHours())) {
            score += CARE_HOURS_WEIGHT;
        }

        return score;
    }

    // ===================== 通用多选项匹配 =====================
    private int calculateMatchScore(String userStr, String catStr, int weight) {
        if (userStr == null || catStr == null) {
            return 0;
        }
        String[] userArr = userStr.split(",");
        for (String item : userArr) {
            if (catStr.contains(item.trim())) {
                return weight;
            }
        }
        return 0;
    }
}