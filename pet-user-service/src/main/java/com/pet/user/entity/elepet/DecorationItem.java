package com.pet.user.entity.elepet;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("decoration_item")
public class DecorationItem {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String type;
    private String itemId;
    private String name;
    private Integer costPoints;
    private String imageUrl;
}