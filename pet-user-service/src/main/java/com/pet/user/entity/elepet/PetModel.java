package com.pet.user.entity.elepet;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("pet_model")
public class PetModel {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String category;
    private String modelName;
    private String svgPath;
    private String defaultColor;
    private String defaultPattern;
}
