package com.pet.common.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CatRecommendDTO {
    private String suitableHousing;    // 适配户型（可选，不选则不筛选）
    private String suitableExperience; // 适配经验（可选）
    private String budgetLevel;         // 预算（可选）
    private String sheddingDegree;     // 掉毛程度（可选）
    private String personalityType;    // 性格类型
    private String requiredCareHours;   // 需要投入的照顾时间
}
