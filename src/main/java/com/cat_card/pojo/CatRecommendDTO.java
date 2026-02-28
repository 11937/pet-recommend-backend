package com.cat_card.pojo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class CatRecommendDTO<T> {
    private Integer suitableHousing;    // 适配户型（可选，不选则不筛选）
    private Integer suitableExperience; // 适配经验（可选）
    private Integer budgetLevel;         // 预算（可选）
    private String sheddingDegree;     // 掉毛程度（可选）
    private String personalityType;    // 性格类型
    private Integer requiredCareHours;   // 需要投入的照顾时间
}
