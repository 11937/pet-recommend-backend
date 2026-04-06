package com.pet.community.schedule;

import com.pet.community.service.CommunityHotSnapshotService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;

/**
 * 每日 00:05（上海时区）重建当日互动榜与顶部 2 卡位。
 */
@Component
@RequiredArgsConstructor
public class CommunityHotDailyScheduler {

    private static final ZoneId SHANGHAI = ZoneId.of("Asia/Shanghai");

    private final CommunityHotSnapshotService hotSnapshotService;

    @Scheduled(cron = "0 5 0 * * ?", zone = "Asia/Shanghai")
    public void rebuildDaily() {
        hotSnapshotService.buildSnapshotForShanghaiDate(LocalDate.now(SHANGHAI));
    }
}
