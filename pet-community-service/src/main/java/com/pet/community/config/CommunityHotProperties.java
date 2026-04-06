package com.pet.community.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** 热度相关可调参数，对应 application.yml 中 community.hot.* */
@Data
@Component
@ConfigurationProperties(prefix = "community.hot")
public class CommunityHotProperties {

    /**
     * 时间衰减指数，越大新帖权重相对越高。
     */
    private double decayGravity = 1.5;

    /**
     * 小时偏移，避免除零并平滑新帖。
     */
    private double decayHourOffset = 2.0;

    /**
     * 参与时间衰减排序的候选帖子数量（按发布时间倒序截取）。
     */
    private int candidateLimit = 500;

    /**
     * 热度区每一维（衰减榜 / 总互动榜）取前 N，默认 100。
     */
    private int poolSize = 100;
}
