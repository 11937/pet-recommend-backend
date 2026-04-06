package com.pet.community.client.dto;

import lombok.Data;

/** 与用户服务 UserBriefVO JSON 对齐，供 Feign 反序列化。 */
@Data
public class UserBriefJson {
    private Long id;
    private String username;
    private String nickName;
    private String avatar;
}
