package com.pet.user.entity.elepet;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("user_creative_points")
public class UserCreativePoints {
    @TableId
    private Long userId;
    private Integer points;
}