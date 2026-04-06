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
    /** 可选：分层图片 URL 的 JSON 数组字符串，自下而上叠放，如 ["/static/a.png","/static/b.png"] */
    private String layerImages;
    private String svgPath;
    private String defaultColor;
    private String defaultPattern;
}
