package com.pet.recommend.service.impl;


import com.alibaba.fastjson2.JSON;
import com.pet.recommend.DTO.UserMatchSaveDTO;
import com.pet.recommend.feign.PetInfoFeignClient;
import com.pet.recommend.feign.UserMatchFeignClient;
import com.pet.recommend.service.PetRecommendService;
import com.pet.common.constant.CacheKeyConstant;
import com.pet.common.dto.Pet;
import com.pet.common.dto.PetRecommendDTO;
import com.pet.common.entity.Result;
import com.pet.common.util.RedisCacheUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;


@Service
@Slf4j // 替换 System.out

public class PetRecommendServiceImpl implements PetRecommendService {

    @Resource(name = "matchSaveExecutor")
    private Executor matchSaveExecutor;  // 改为 Spring 管理的线程池

    @Resource
    private UserMatchFeignClient userMatchFeignClient;

    @Resource
    private PetInfoFeignClient petInfoFeignClient;

    @Resource
    private RedisCacheUtil redisCacheUtil;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /** 远程调用次数统计 */
    private final AtomicInteger remoteCallCount = new AtomicInteger(0);

    /** 随机数（防雪崩） */
    private static final Random RANDOM = new Random();

    // ===================== 推荐权重常量（总分100） =====================
    public static final int SPACE = 20;        // 空间需求
    public static final int TIME = 15;          // 时间需求
    public static final int BUDGET = 15;        // 预算
    public static final int INTERACTION = 10;   // 互动性
    public static final int BEGINNER = 10;      // 新手友好度
    public static final int TEMP = 5;            // 温度控制
    public static final int HUMIDITY = 2;        // 湿度控制（原5，匀出3给食性）
    public static final int NOISE = 3;           // 噪音容忍（原5，匀出2给食性）
    public static final int UVB = 5;              // UVB 需求
    public static final int DIET = 5;              // 食性偏好

    /** 空值缓存标记 */
    private static final List<Pet> EMPTY_CAT_LIST = new ArrayList<>(0);

    @Override
    public List<Pet> recommendPets(PetRecommendDTO dto) {
        String cacheKey = CacheKeyConstant.PET_RECOMMEND_PREFIX;
        log.info("【Redis】生成缓存 key：{}", cacheKey);

        // 1. 查询缓存
        List<Pet> petList = getCache(cacheKey);
        if (petList != null) {
            // 缓存是空值标记，直接返回空
            if (petList.isEmpty()) {
                log.info("【Redis】缓存空值命中，直接返回空列表");
                return new ArrayList<>();
            }
            log.info("【Redis】全量品种缓存命中，数量：{}", petList.size());
        }

        // 2. 缓存未命中 → 加锁查询远程（防止缓存击穿）
        if (petList == null) {
            petList = loadDataFromRemoteWithLock(cacheKey);
        }


        // ---------- 新增：硬性过滤 ----------
        List<Pet> filteredList = petList.stream()
                .filter(pet -> filterByHardConditions(pet, dto))
                .collect(Collectors.toList());

        log.info("硬性过滤后剩余品种数量：{}", filteredList.size());

        // 5. 如果过滤后无结果，返回默认推荐
        if (filteredList.isEmpty()) {
            log.info("硬性过滤后无符合条件品种，返回默认热门推荐");
            return getDefaultRecommendations(petList);
        }

        // 个性化打分排序
        List<Pet> result = filteredList.stream()
                .peek(pet -> pet.setScore(calculateScore(pet, dto)))
                .sorted((c1, c2) -> Integer.compare(c2.getScore(), c1.getScore()))
                .limit(10)
                .collect(Collectors.toList());

        // 如果打分后无结果（例如所有品种得分均为0），也返回默认推荐
        if (result.isEmpty()) {
            log.info("个性化推荐无结果，返回默认热门推荐");
            return getDefaultRecommendations(petList);
        }

        // 获取当前用户 ID（从 SecurityContext 或请求上下文获取）
        Long userId = getCurrentUserId();
        log.info("当前登录用户ID: {}", userId);
        if (userId != null) {
            // 异步保存匹配记录
            CompletableFuture.runAsync(() -> {
                try {
                    UserMatchSaveDTO saveDTO = new UserMatchSaveDTO();
                    saveDTO.setMatchParams(JSON.toJSONString(dto));
                    List<Long> breedIds = result.stream().map(Pet::getId).collect(Collectors.toList());
                    saveDTO.setResult(JSON.toJSONString(breedIds));
                    // 调用 Feign 客户端
                    Result<Void> res = userMatchFeignClient.saveMatch(saveDTO, userId);
                    if (res.getCode() != 200) {
                        log.error("保存匹配记录失败，返回码：{}", res.getCode());
                    }
                } catch (Exception e) {
                    log.error("调用 user 服务保存匹配记录异常", e);
                }
            }, matchSaveExecutor);
        }

        return result;
    }

    // ===================== 硬性过滤条件（加强版） =====================
    private boolean filterByHardConditions(Pet pet, PetRecommendDTO dto) {
        // 1. 法律状态：必须合法
        if (pet.getLegalStatus() == null || pet.getLegalStatus() != 1) {
            return false;
        }

        // 2. UVB：用户不愿提供UVB则排除需要UVB的品种
        if (Boolean.FALSE.equals(dto.getHasUVB()) && pet.getNeedUvb() != null && pet.getNeedUvb() == 1) {
            return false;
        }

        // 3. 空间下限：用户空间评分必须 ≥ 品种需求 - 1
        if (dto.getLivingSpace() != null && pet.getSpaceRequirement() != null) {
            if (dto.getLivingSpace() < pet.getSpaceRequirement() - 1) {
                return false;
            }
        }

        // 4. 温度硬性过滤：用户温控能力极低（≤1）时，淘汰温度范围过大的品种
        if (dto.getTempControl() != null && dto.getTempControl() <= 1) {
            if (pet.getTempMin() != null && pet.getTempMax() != null) {
                double range = pet.getTempMax().subtract(pet.getTempMin()).doubleValue();
                if (range > 15) { // 跨度大于15℃认为需要较强温控能力
                    return false;
                }
            }
        }

        // 5. 湿度硬性过滤：用户湿度控制能力极低（≤1）时，淘汰湿度范围过大的品种
        if (dto.getHumidityControl() != null && dto.getHumidityControl() <= 1) {
            if (pet.getHumidityMin() != null && pet.getHumidityMax() != null) {
                int range = pet.getHumidityMax() - pet.getHumidityMin();
                if (range > 20) { // 湿度跨度大于20%认为需要较强控制能力
                    return false;
                }
            }
        }
        return true;
    }

    // ===================== 缓存获取（含空值解析） =====================
    private List<Pet> getCache(String cacheKey) {
        try {
            String cacheValue = redisCacheUtil.getCache(cacheKey);
            if (cacheValue == null || cacheValue.isEmpty()) {
                return null;
            }
            return JSON.parseArray(cacheValue, Pet.class);
        } catch (Exception e) {
            log.error("Redis 缓存查询异常", e);
            return null;
        }
    }

    // ===================== 带分布式锁的远程加载（防击穿） =====================
    private List<Pet> loadDataFromRemoteWithLock(String cacheKey) {
        // 锁 key
        String lockKey = CacheKeyConstant.PET_RECOMMEND_LOCK;
        try {
            // 尝试加锁（5秒自动释放）
            Boolean lock = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", 5, java.util.concurrent.TimeUnit.SECONDS);
            if (Boolean.FALSE.equals(lock)) {
                // 未获取到锁，直接返回空（或重试）
                log.warn("未获取到分布式锁，直接返回空");
                return new ArrayList<>();
            }

            // 再次检查缓存（双重检查）
            List<Pet> cachePetList = getCache(cacheKey);
            if (cachePetList != null) {
                return cachePetList.isEmpty() ? new ArrayList<>() : cachePetList;
            }

            // 真正执行远程调用
            return loadDataFromRemote(cacheKey);
        } finally {
            // 释放锁
            redisTemplate.delete(lockKey);
        }
    }

    // ===================== 真正远程调用 + 写入缓存 =====================
    private List<Pet> loadDataFromRemote(String cacheKey) {
        log.info("【远程调用】开始调用 pet-info 服务，累计调用次数：{}", remoteCallCount.incrementAndGet());

        try {
            // 远程调用
            Result<List<Pet>> result = petInfoFeignClient.findAllPetsList();
            if (result == null || result.getCode() != 200 || result.getData() == null) {
                log.error("【远程调用】服务异常或无数据");
                // 缓存空值（防穿透）
                setCacheWithNull(cacheKey);
                return new ArrayList<>();
            }

            List<Pet> petList = result.getData();
            if (petList.isEmpty()) {
                setCacheWithNull(cacheKey);
                return new ArrayList<>();
            }

            // 写入缓存（随机过期时间防雪崩）
            long expire = CacheKeyConstant.CACHE_EXPIRE_BASE
                    + RANDOM.nextInt((int) CacheKeyConstant.CACHE_EXPIRE_RANDOM);

            redisCacheUtil.setCache(cacheKey, JSON.toJSONString(petList), expire);
            log.info("【Redis】✅ 全量品种写入缓存，数量：{}，过期时间：{}秒", petList.size(), expire);

            return petList;
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
    private int calculateScore(Pet pet, PetRecommendDTO dto) {
        double total = 0.0;

        total += spaceMatch(dto.getLivingSpace(), pet.getSpaceRequirement()) * SPACE;
        total += timeMatch(dto.getDailyTime(), pet.getTimeRequirement()) * TIME;
        total += budgetMatch(dto.getMonthlyBudget(), pet.getBudgetMin(), pet.getBudgetMax()) * BUDGET;
        total += interactionMatch(dto.getInteractionLevel(), pet.getInteractionLevel()) * INTERACTION;
        total += beginnerMatch(dto.getExperienceLevel(), pet.getBeginnerFriendly()) * BEGINNER;
        total += tempMatch(dto.getTempControl(), pet.getTempMin(), pet.getTempMax()) * TEMP;
        total += humidityMatch(dto.getHumidityControl(), pet.getHumidityMin(), pet.getHumidityMax()) * HUMIDITY;
        total += noiseMatch(dto.getNoiseTolerance(), pet.getNoiseLevel()) * NOISE;
        total += uvbMatch(dto.getHasUVB(), pet.getNeedUvb()) * UVB;
        total += dietMatch(dto.getDietPreference(), pet.getDietType()) * DIET; // 新增食性匹配

        return (int) Math.round(total * 100);
    }

    // ===================== 各维度匹配度计算 =====================

    private double spaceMatch(Integer userSpace, Integer petSpace) {
        if (userSpace == null || petSpace == null) return 0.5;
        int diff = Math.abs(userSpace - petSpace);
        return Math.max(0, 1 - diff / 5.0);
    }

    private double timeMatch(Integer userTime, Integer petTime) {
        if (userTime == null || petTime == null) return 0.5;
        int diff = Math.abs(userTime - petTime);
        return Math.max(0, 1 - diff / 5.0);
    }

    private double budgetMatch(BigDecimal userBudget, BigDecimal petMin, BigDecimal petMax) {
        if (userBudget == null) return 0.5;
        if (petMin == null || petMax == null) return 0.5;
        // 防止除零
        if (petMin.compareTo(BigDecimal.ZERO) == 0 || petMax.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }
        if (userBudget.compareTo(petMin) >= 0 && userBudget.compareTo(petMax) <= 0) {
            return 1.0;
        }
        if (userBudget.compareTo(petMin) < 0) {
            return userBudget.divide(petMin, 4, RoundingMode.HALF_UP).doubleValue();
        }
        return petMax.divide(userBudget, 4, RoundingMode.HALF_UP).doubleValue();
    }

    private double interactionMatch(Integer userInteraction, Integer petInteraction) {
        if (userInteraction == null || petInteraction == null) return 0.5;
        int diff = Math.abs(userInteraction - petInteraction);
        return Math.max(0, 1 - diff / 5.0);
    }

    private double beginnerMatch(Integer userExp, Integer petBeginner) {
        if (userExp == null || petBeginner == null) return 0.5;
        // 新手友好度硬性处理：新手遇到新手友好度低于4的品种直接得0分
        if (userExp == 1 && petBeginner < 4) {
            return 0.0;
        }
        int expectedMin;
        switch (userExp) {
            case 1: expectedMin = 4; break;
            case 2: expectedMin = 3; break;
            default: expectedMin = 1; break;
        }
        if (petBeginner >= expectedMin) {
            return 1.0;
        } else {
            return petBeginner / (double) expectedMin;
        }
    }

    private double tempMatch(Integer userTempControl, BigDecimal petTempMin, BigDecimal petTempMax) {
        if (userTempControl == null || petTempMin == null || petTempMax == null) return 0.5;
        double range = petTempMax.subtract(petTempMin).doubleValue();
        double abilityRange = 5 + (userTempControl - 1) * 3.75; // 5~20
        return Math.min(1.0, abilityRange / Math.max(1.0, range));
    }

    private double humidityMatch(Integer userHumidityControl, Integer petHumidityMin, Integer petHumidityMax) {
        if (userHumidityControl == null || petHumidityMin == null || petHumidityMax == null) return 0.5;
        int range = petHumidityMax - petHumidityMin;
        double abilityRange = 10 + (userHumidityControl - 1) * 7.5; // 10~40
        return Math.min(1.0, abilityRange / Math.max(1.0, range));
    }

    private double noiseMatch(Integer userNoise, Integer petNoise) {
        if (userNoise == null || petNoise == null) return 0.5;
        int diff = Math.abs(userNoise - petNoise);
        return Math.max(0, 1 - diff / 5.0);
    }

    private double uvbMatch(Boolean hasUVB, Integer needUvb) {
        if (Boolean.TRUE.equals(hasUVB)) return 1.0;
        return (needUvb != null && needUvb == 0) ? 1.0 : 0.0;
    }

    /** 食性匹配度 */
    private double dietMatch(String userPref, String petDiet) {
        // 用户未指定偏好，给中等分（0.5）
        if (userPref == null || userPref.trim().isEmpty()) {
            return 0.5;
        }
        // 品种食性未知，给 0 分
        if (petDiet == null || petDiet.trim().isEmpty()) {
            return 0.0;
        }

        // 将逗号分隔的字符串转为集合（去除空格）
        Set<String> prefSet = new HashSet<>(Arrays.asList(userPref.split("\\s*,\\s*")));
        Set<String> petSet = new HashSet<>(Arrays.asList(petDiet.split("\\s*,\\s*")));

        // 计算交集
        Set<String> intersection = new HashSet<>(prefSet);
        intersection.retainAll(petSet);
        if (intersection.isEmpty()) {
            return 0.0; // 无共同标签，完全不匹配
        }

        // 计算并集
        Set<String> union = new HashSet<>(prefSet);
        union.addAll(petSet);

        // 相似度 = 交集大小 / 并集大小
        return intersection.size() / (double) union.size();
    }

    /**
     * 获取默认推荐列表（当个性化推荐无结果时调用）
     * @param allPets 全量品种列表
     * @return 默认推荐的前10个品种
     */
    private List<Pet> getDefaultRecommendations(List<Pet> allPets) {
        if (allPets == null || allPets.isEmpty()) {
            return new ArrayList<>();
        }
        // 按新手友好度降序排序（优先推荐新手友好度高的品种），取前10
        return allPets.stream()
                .sorted(Comparator.comparing(Pet::getBeginnerFriendly,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(10)
                .collect(Collectors.toList());
    }

    private Long getCurrentUserId() {

        // 从 SecurityContextHolder 获取，或者从请求中解析 token 得到
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Long) {
            return (Long) authentication.getPrincipal();
        }
        return null;
    }

}