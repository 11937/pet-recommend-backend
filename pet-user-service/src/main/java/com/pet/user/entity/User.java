package com.pet.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/** 用户表实体；role 与库字段一致，供登录态与内部服务鉴权（如社区管理员）。 */
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
    /** USER / ADMIN，与库字段 role 对应 */
    private String role;
    private Integer VipLevel;
    private String vipExpire;


    public void setNickname(String nickName) {
        nickName=this.nickName;
    }
}
