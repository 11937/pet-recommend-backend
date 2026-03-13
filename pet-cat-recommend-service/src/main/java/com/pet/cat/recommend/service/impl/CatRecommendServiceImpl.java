package com.pet.cat.recommend.service.impl;


import com.pet.cat.recommend.feign.CatInfoFeignClient;
import com.pet.cat.recommend.service.CatRecommendService;
import com.pet.common.dto.Cat;
import com.pet.common.dto.CatRecommendDTO;

import com.pet.common.entity.Result;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;

import java.util.List;
import java.util.stream.Collectors;


// @Service注解
@Service
public class CatRecommendServiceImpl implements CatRecommendService {

    @Resource
    private CatInfoFeignClient catInfoFeignClient;

    @Override
    public List<Cat> recommendCats(CatRecommendDTO dto) {

        // 1. 从猫信息服务获取所有猫
        Result<List<Cat>> result = catInfoFeignClient.findAllCatList();
        List<Cat> catList = result.getData();

        if (catList == null || catList.isEmpty()) {
            return new ArrayList<>();
        }

        // 2. 对每只猫计算匹配分
        List<Cat> recommendList = catList.stream()
                .peek(cat -> {
                    // 计算总分
                    int score = calculateScore(cat, dto);
                    // 把分数 set 给猫（你实体里加个 score 字段即可）
                    cat.setScore(score);
                })
                // 3. 按分数从高到低排序
                .sorted((c1, c2) -> Integer.compare(c2.getScore(), c1.getScore()))
                // 4. 取前10只
                .limit(10)
                .collect(Collectors.toList());

        return recommendList;
    }


     // ===================== 推荐得分算法=====================//
    private int calculateScore(Cat cat, CatRecommendDTO dto) {
        int score = 0;
        // ===================== 排错打印 =====================
        System.out.println("==================================");
        System.out.println("用户传入DTO = " + dto);
        System.out.println("猫咪信息 = " + cat);
        // =====================================================
        // 条件权重
        final int CARE_HOURS_WEIGHT = 40;
        final int HOUSE_TYPE_WEIGHT = 20;
        final int EXPERIENCE_WEIGHT = 50;
        final int BUDGET_WEIGHT = 30;
        final int SHEDDING_WEIGHT = 40;
        final int PERSONALITY_WEIGHT = 10;

        // 居住环境匹配
        if (dto.getSuitableHousing() != null && dto.getSuitableHousing().equals(cat.getSuitableHousing())) {
            score += HOUSE_TYPE_WEIGHT;
        }

        // 养宠经验匹配
        if (dto.getSuitableExperience() != null && dto.getSuitableExperience().equals(cat.getSuitableExperience())) {
            score += EXPERIENCE_WEIGHT;
        }

        // 预算匹配
        if (dto.getBudgetLevel() != null && dto.getBudgetLevel().equals(cat.getBudgetLevel())) {
            score += BUDGET_WEIGHT;
        }

        // 性格匹配（支持多个选项用逗号分隔）
        if (dto.getPersonalityType() != null && cat.getPersonalityType() != null) {
            String[] userPersonality = dto.getPersonalityType().split(",");
            for (String p : userPersonality) {
                if (cat.getPersonalityType().contains(p.trim())) {
                    score += PERSONALITY_WEIGHT;
                    break;
                }
            }
        }

        // 掉毛程度匹配
        if (dto.getSheddingDegree() != null && cat.getSheddingDegree() != null) {
            String[] userShedding = dto.getSheddingDegree().split(",");
            for (String s : userShedding) {
                if (cat.getSheddingDegree().contains(s.trim())) {
                    score += SHEDDING_WEIGHT;
                    break;
                }
            }
        }

        // 陪伴时间匹配
        if (dto.getRequiredCareHours() != null && dto.getRequiredCareHours().equals(cat.getRequiredCareHours())) {
            score += CARE_HOURS_WEIGHT;
        }

        return score;
    }


}