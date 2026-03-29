package com.pet.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String phone;
    private String password;
    private String username;
    private String avatar;
    private LocalDateTime createdAt;
    private String nickName;
    private Integer VipLevel;
    private String vipExpire;


    public void setNickname(String nickName) {
        nickName=this.nickName;
    }
}
