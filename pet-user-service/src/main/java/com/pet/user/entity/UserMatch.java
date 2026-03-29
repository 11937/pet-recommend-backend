package com.pet.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_match_record")
public class UserMatch {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String matchParams;   // JSON字符串
    private String result;        // JSON字符串
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}