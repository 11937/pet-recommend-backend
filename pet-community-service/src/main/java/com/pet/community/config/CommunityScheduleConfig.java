package com.pet.community.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * 为 @Scheduled 提供 TaskScheduler，避免启动时告警且保证日榜任务可执行。
 */
@Configuration
public class CommunityScheduleConfig {

    @Bean
    public ThreadPoolTaskScheduler communityTaskScheduler() {
        ThreadPoolTaskScheduler s = new ThreadPoolTaskScheduler();
        s.setPoolSize(2);
        s.setThreadNamePrefix("community-schedule-");
        s.initialize();
        return s;
    }
}
