package com.pet.common.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class PetRecommendDTO {
    private String dietPreference;     // 食性
    private Integer livingSpace;       // 空间评分 1-5
    private Integer dailyTime;         // 时间评分 1-5
    private BigDecimal monthlyBudget;  // 月预算（元）
    private Integer interactionLevel;  // 期望互动性 1-5
    private Integer experienceLevel;   // 养宠经验 1-3（新手/中级/资深）
    private Integer noiseTolerance;    // 噪音容忍度 1-5
    private Integer tempControl;       // 温控能力 1-5
    private Integer humidityControl;   // 湿度控制能力 1-5
    private Boolean hasUVB;            // 是否愿意提供UVB设备
}
