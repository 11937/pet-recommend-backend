package com.cat_card.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

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




}
