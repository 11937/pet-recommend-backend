package com.pet.user.entity.elepet;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_decoration")
public class UserDecoration {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String decorationType;
    private String itemId;
    private LocalDateTime createdAt;
}
