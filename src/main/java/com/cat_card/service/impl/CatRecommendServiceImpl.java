package com.cat_card.service.impl;

import com.cat_card.mapper.CatRecommendMapper;
import com.cat_card.pojo.Cat;
import com.cat_card.pojo.CatRecommendDTO;
import com.cat_card.service.CatRecommendService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

// 注意：加@Service注解，让Spring扫描并管理这个类
@Service
public class CatRecommendServiceImpl implements CatRecommendService {


    @Autowired
    private CatRecommendMapper catRecommendMapper;

    // 条件权重
    private final int CARE_HOURS_WEIGHT = 4;
    private final int HOUSE_TYPE_WEIGHT = 2;
    private final int EXPERIENCE_WEIGHT = 5;
    private final int BUDGET_WEIGHT = 3;
    private final int SHEDDING_WEIGHT = 4;
    private final int PERSONALITY_WEIGHT = 1;

    @Override
    public List<Cat> recommendCats(CatRecommendDTO dto) {
        // 1. 查询所有猫咪数据（从数据库）
        List<Cat> allCatList = catRecommendMapper.selectAllCats();

        // 2. 初始化多值列表（把数据库单值转成业务多值列表，核心！）
        initMultiValueList(allCatList);

        // 3. 第一步：严格匹配所有筛选条件
        List<Cat> strictMatchList = filterCatsByDTO(allCatList, dto);
        if (!strictMatchList.isEmpty()) {
            // 打分+排序后返回
            return calculateScoreAndSort(strictMatchList, dto);
        }

        // 4. 第二步：降级1 → 只保留核心条件（户型+经验）
        CatRecommendDTO degrade1DTO = copyAndClearNonCoreFields(dto);
        List<Cat> degrade1List = filterCatsByDTO(allCatList, degrade1DTO);
        if (!degrade1List.isEmpty()) {
            return calculateScoreAndSort(degrade1List, dto);
        }

        // 5. 第三步：降级2 → 只保留最核心条件（户型）
        CatRecommendDTO degrade2DTO = copyAndClearOnlyKeepHousing(dto);
        List<Cat> degrade2List = filterCatsByDTO(allCatList, degrade2DTO);
        if (!degrade2List.isEmpty()) {
            return calculateScoreAndSort(degrade2List, dto);
        }

        // 6. 最终兜底 → 返回所有猫咪（打分排序）
        return calculateScoreAndSort(allCatList, dto);
    }

    /**
     * 初始化多值列表：把数据库单值字段转成业务多值列表（适配推荐逻辑）
     */
    private void initMultiValueList(List<Cat> catList) {
        for (Cat cat : catList) {
            // 户型：单值转列表（比如 suitableHousing=2 → [2]）
            if (cat.getSuitableHousing() != null) {
                cat.setSuitableHousingList(Collections.singletonList(cat.getSuitableHousing()));
            } else {
                cat.setSuitableHousingList(new ArrayList<>());
            }

            // 经验：单值转列表
            if (cat.getSuitableExperience() != null) {
                cat.setSuitableExperienceList(Collections.singletonList(cat.getSuitableExperience()));
            } else {
                cat.setSuitableExperienceList(new ArrayList<>());
            }

            // 预算：单值转列表
            if (cat.getBudgetLevel() != null) {
                cat.setBudgetLevelList(Collections.singletonList(cat.getBudgetLevel()));
            } else {
                cat.setBudgetLevelList(new ArrayList<>());
            }

            // 掉毛程度：单值转列表
            if (cat.getSheddingDegree() != null) {
                cat.setSheddingDegreeList(Collections.singletonList(cat.getSheddingDegree()));
            } else {
                cat.setSheddingDegreeList(new ArrayList<>());
            }

            // 性格类型：单值转列表
            if (cat.getPersonalityType() != null) {
                cat.setPersonalityTypeList(Collections.singletonList(cat.getPersonalityType()));
            } else {
                cat.setPersonalityTypeList(new ArrayList<>());
            }

            // 照顾时间：单值转列表
            if (cat.getRequiredCareHours() != null) {
                cat.setRequiredCareHoursList(Collections.singletonList(cat.getRequiredCareHours()));
            } else {
                cat.setRequiredCareHoursList(new ArrayList<>());
            }

            // 初始化热门度（默认3，可根据品种手动调整）
            if (cat.getPopularity() == null) {
                cat.setPopularity(3);
            }
        }
    }

    /**
     * 根据DTO筛选猫咪列表（核心匹配逻辑）
     */
    private List<Cat> filterCatsByDTO(List<Cat> allCatList, CatRecommendDTO dto) {
        return allCatList.stream()
                .filter(cat -> matchHousing(cat, dto))
                .filter(cat -> matchExperience(cat, dto))
                .filter(cat -> matchBudget(cat, dto))
                .filter(cat -> matchShedding(cat, dto))
                .filter(cat -> matchPersonality(cat, dto))
                .filter(cat -> matchCareHours(cat, dto))
                .collect(Collectors.toList());
    }

    // 户型匹配
    private boolean matchHousing(Cat cat, CatRecommendDTO dto) {
        if (dto.getSuitableHousing() == null) {
            return true; // 无筛选条件，全部匹配
        }
        return cat.getSuitableHousingList() != null
                && cat.getSuitableHousingList().contains(dto.getSuitableHousing());
    }

    // 经验匹配
    private boolean matchExperience(Cat cat, CatRecommendDTO dto) {
        if (dto.getSuitableExperience() == null) {
            return true;
        }
        return cat.getSuitableExperienceList() != null
                && cat.getSuitableExperienceList().contains(dto.getSuitableExperience());
    }

    // 预算匹配
    private boolean matchBudget(Cat cat, CatRecommendDTO dto) {
        if (dto.getBudgetLevel() == null) {
            return true;
        }
        return cat.getBudgetLevelList() != null
                && cat.getBudgetLevelList().contains(dto.getBudgetLevel());
    }

    // 掉毛程度匹配（兼容前端String类型）
    private boolean matchShedding(Cat cat, CatRecommendDTO dto) {
        Integer shedding = safeStringToInt(dto.getSheddingDegree());
        if (shedding == null) {
            return true;
        }
        return cat.getSheddingDegreeList() != null
                && cat.getSheddingDegreeList().contains(shedding);
    }

    // 性格类型匹配（兼容前端String类型）
    private boolean matchPersonality(Cat cat, CatRecommendDTO dto) {
        Integer personality = safeStringToInt(dto.getPersonalityType());
        if (personality == null) {
            return true;
        }
        return cat.getPersonalityTypeList() != null
                && cat.getPersonalityTypeList().contains(personality);
    }

    // 照顾时间匹配
    private boolean matchCareHours(Cat cat, CatRecommendDTO dto) {
        if (dto.getRequiredCareHours() == null) {
            return true;
        }
        return cat.getRequiredCareHoursList() != null
                && cat.getRequiredCareHoursList().contains(dto.getRequiredCareHours());
    }

    /**
     * 权重打分 + 排序（核心推荐逻辑）
     */
    private List<Cat> calculateScoreAndSort(List<Cat> catList, CatRecommendDTO dto) {
        // 1. 计算匹配分数
        for (Cat cat : catList) {
            int totalScore = 0;

            // 户型加分
            if (matchHousing(cat, dto)) {
                totalScore += HOUSE_TYPE_WEIGHT;
            }

            // 经验加分
            if (matchExperience(cat, dto)) {
                totalScore += EXPERIENCE_WEIGHT;
            }

            // 预算加分
            if (matchBudget(cat, dto)) {
                totalScore += BUDGET_WEIGHT;
            }

            // 掉毛程度加分
            if (matchShedding(cat, dto)) {
                totalScore += SHEDDING_WEIGHT;
            }

            // 性格类型加分
            if (matchPersonality(cat, dto)) {
                totalScore += PERSONALITY_WEIGHT;
            }

            // 照顾时间加分
            if (matchCareHours(cat, dto)) {
                totalScore += CARE_HOURS_WEIGHT;
            }

            // 热门度加分（默认3，热门品种可手动设为4/5）
            totalScore += cat.getPopularity();

            // 赋值匹配分数
            cat.setMatchScore(totalScore);
        }

        // 2. 按分数排序（兼容null值，降序）
        catList.sort(Comparator.comparing(Cat::getMatchScore,
                        Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(Cat::getPopularity,
                        Comparator.nullsLast(Comparator.reverseOrder())));

        return catList;
    }

    /**
     * 复制DTO并清空非核心字段（降级1：只保留户型+经验）
     */
    private CatRecommendDTO copyAndClearNonCoreFields(CatRecommendDTO dto) {
        CatRecommendDTO degradeDTO = new CatRecommendDTO();
        BeanUtils.copyProperties(dto, degradeDTO);
        // 清空非核心字段
        degradeDTO.setBudgetLevel(null);
        degradeDTO.setSheddingDegree(null);
        degradeDTO.setPersonalityType(null);
        degradeDTO.setRequiredCareHours(null);
        return degradeDTO;
    }

    /**
     * 复制DTO并只保留户型（降级2）
     */
    private CatRecommendDTO copyAndClearOnlyKeepHousing(CatRecommendDTO dto) {
        CatRecommendDTO degradeDTO = new CatRecommendDTO();
        BeanUtils.copyProperties(dto, degradeDTO);
        // 只保留户型，清空其他所有字段
        degradeDTO.setSuitableExperience(null);
        degradeDTO.setBudgetLevel(null);
        degradeDTO.setSheddingDegree(null);
        degradeDTO.setPersonalityType(null);
        degradeDTO.setRequiredCareHours(null);
        return degradeDTO;
    }

    /**
     * 安全转换String到Integer（兼容前端传参）
     */
    private Integer safeStringToInt(String str) {
        if (str == null || str.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(str.trim());
        } catch (NumberFormatException e) {
            // 前端传非数字，返回null不参与匹配
            return null;
        }
    }
}