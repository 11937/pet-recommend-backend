package com.pet.user.dto;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("user_match")
public class UserMatchSaveDTO {
    private String matchParams; // 推荐条件 JSON
    private String result;
}