package com.pet.user.vo;

import lombok.Data;

@Data
public class UserSocialStatsVO {
    private long followerCount;
    private long followingCount;
    /** 当前登录用户是否已关注该用户；未登录时为 null */
    private Boolean followingByMe;
}
