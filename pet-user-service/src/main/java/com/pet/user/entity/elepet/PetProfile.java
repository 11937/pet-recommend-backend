package com.pet.user.entity.elepet;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("pet_profile")
public class PetProfile {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String name;
    private Long breedId;
    private String avatar;
    private LocalDate birthday;
    private String decoration; // JSON 字符串
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    private String category;
}