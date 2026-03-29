package com.pet.common.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("breed")
public class Pet {
    @TableId(type = IdType.AUTO)
    private Long id; // 主键ID

    private String name; // 品种名称

    private String scientificName; // 学名

    private String category; // 大类

    private String subCategory; // 子类

    private String dietType;   // 食性

    private Integer spaceRequirement; // 空间需求 1-5分

    private String spaceDesc; // 空间描述

    private Integer timeRequirement; // 时间需求 1-5分

    private BigDecimal budgetMin; // 最低月均预算

    private BigDecimal budgetMax; // 最高月均预算

    private BigDecimal tempMin; // 最低温度要求

    private BigDecimal tempMax; // 最高温度要求

    private Integer humidityMin; // 最低湿度要求

    private Integer humidityMax; // 最高湿度要求

    private Integer needUvb; // 是否需要UVB灯 0-不需要 1-需要

    private Integer interactionLevel; // 互动性 1-5分

    private Integer noiseLevel; // 噪音等级 1-5分

    private Integer beginnerFriendly; // 新手友好度 1-5分

    private String lifespan; // 寿命

    private Integer legalStatus; // 法律状态 1-合法 0-需许可 -1-禁止

    private String legalDesc; // 法律说明

    private String specialNeeds; // 特殊需求 JSON格式

    private String commonHealthIssues; // 常见健康问题

    private String description; // 品种简介

    private String coverImage; // 封面图片URL

    private LocalDateTime createdAt; // 创建时间

    private LocalDateTime updatedAt; // 更新时间

    private Integer score;  // 推荐得分
}