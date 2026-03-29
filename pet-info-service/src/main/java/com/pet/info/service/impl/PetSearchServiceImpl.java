package com.pet.info.service.impl;

import com.alibaba.fastjson2.JSON;
import com.pet.info.mapper.PetSearchMapper;
import com.pet.info.service.PetSearchService;
import com.pet.common.constant.CacheKeyConstant;
import com.pet.common.dto.Pet;
import com.pet.common.util.RedisCacheUtil;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;
import java.util.Random;

@Service
public class PetSearchServiceImpl implements PetSearchService {
    // 注入mapper接口（推荐用@Resource，和@Autowired等效，更规范）
    @Resource
    private PetSearchMapper petSearchMapper;

    // 注入缓存工具类
    @Resource
    private RedisCacheUtil redisCacheUtil;

    // 随机数生成器（解决缓存雪崩）
    private static final Random RANDOM = new Random();

    @Override
    public List<Pet> findPetList(Pet pet) {

        // 1. 构造缓存key：前缀 + Cat对象JSON的MD5（适配你的多参数入参）
        String paramJson = JSON.toJSONString(pet);
        String md5Param = RedisCacheUtil.md5(paramJson);
        String cacheKey = CacheKeyConstant.PET_LIST_PREFIX + md5Param;


        // ===== 添加日志1：打印生成的缓存key =====
        System.out.println("【Redis】生成的缓存key：" + cacheKey);

        // 2. 缓存操作加try-catch：Redis异常时自动降级直连MySQL
        try {
            String cacheValue = redisCacheUtil.getCache(cacheKey);
            // 优化空值判断：trim()避免空格问题
            if (cacheValue != null && !cacheValue.trim().isEmpty()) {
                // 缓存命中：解析后直接return，跳过数据库查询
                System.out.println("【Redis】✅ 缓存命中！key=" + cacheKey);
                List<Pet> petList = JSON.parseArray(cacheValue, Pet.class);
                return petList; // 关键：加return，让缓存生效
            }
            System.out.println("【Redis】❌ 缓存未命中，key=" + cacheKey);
        } catch (Exception e) {
            System.err.println("Redis缓存异常，自动降级直连数据库：" + e.getMessage());
            e.printStackTrace(); // 可选：打印异常栈，方便排查
        }

        // 3. 缓存未命中/Redis异常：执行数据库查询
        List<Pet> petList = petSearchMapper.findPetList(pet);
        System.out.println("【数据库】查询结果条数：" + (petList == null ? 0 : petList.size()));


        // 5. 回写Redis缓存（异常时忽略）
        try {
            long expireTime = CacheKeyConstant.CACHE_EXPIRE_BASE + RANDOM.nextInt((int) CacheKeyConstant.CACHE_EXPIRE_RANDOM);
            redisCacheUtil.setCache(cacheKey, JSON.toJSONString(petList), expireTime);
            System.out.println("【Redis】✅ 数据已写入缓存，key=" + cacheKey + "，过期时间=" + expireTime + "秒");
        } catch (Exception e) {
            System.err.println("Redis缓存回写失败，忽略：" + e.getMessage());
        }

        // 6. 返回你的原有结果
        return petList;
    }
}