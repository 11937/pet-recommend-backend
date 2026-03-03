package com.cat_card.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.List;

/**
 * 猫咪品种实体类（对应 cat_recommend_system 库的 cat_characteristic 表）
 */
@Data
@TableName("cat_characteristic") // 绑定数据库表名，必须与实际表名一致
public class Cat {
    // 主键ID（自增）
    @TableId(type = IdType.AUTO)
    private Long id;

    // 猫咪品种名称（如：布偶猫）
    private String breedName;

    private String breedType;

    // 适配居住环境（1=小户型 2=中户型 3=大户型）
    private Integer suitableHousing;

    // 每日所需照顾时间（1=少量 2=中等 3=大量）
    private Integer requiredCareHours;

    // 适合养宠经验（1=新手 2=有经验 3=资深）
    private Integer suitableExperience;

    // 预算等级（1=低 2=中 3=高）
    private Integer budgetLevel;

    // 掉毛程度（1=轻微 2=中等 3=严重）
    private Integer sheddingDegree;

    // 性格类型（1=粘人 2=活泼 3=高冷）
    private Integer personalityType;

    //品种特点（长文本
    private String breedFeatures;

    // 性格特征
    private String personalityTraits;

    // 饲养建议
    private String feedingSuggestions;

    // 健康注意
    private String healthAttention;

    // 互动需求
    private String interactionNeeds;

    // 预算参考（如 “3000-8000 元”）
    private String budgetReference;

    // 猫咪图片地址
    private String imgUrl;

    // ===================== 业务逻辑字段（多值适配，不映射到数据库） =====================
    // 核心：用 @TableField(exist = false) 标记，MyBatis-Plus 会忽略这些字段的数据库映射
    // 适配户型列表（如 [2,3]，支持多值适配）
    @TableField(exist = false)
    private List<Integer> suitableHousingList;
    // 适配经验列表
    @TableField(exist = false)
    private List<Integer> suitableExperienceList;
    // 预算等级列表
    @TableField(exist = false)
    private List<Integer> budgetLevelList;
    // 掉毛程度列表
    @TableField(exist = false)
    private List<Integer> sheddingDegreeList;
    // 性格类型列表
    @TableField(exist = false)
    private List<Integer> personalityTypeList;
    // 照顾时间列表
    @TableField(exist = false)
    private List<Integer> requiredCareHoursList;

    // 推荐逻辑所需字段（不映射数据库）
    @TableField(exist = false)
    private Integer matchScore;    // 匹配分数
    @TableField(exist = false)
    private Integer popularity = 3; // 热门度（默认3，热门品种手动设为4/5）
}
