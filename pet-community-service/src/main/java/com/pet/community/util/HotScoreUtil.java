package com.pet.community.util;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;

/** Feed 热度计算：衰减分与赞+藏总和工具方法。 */
public final class HotScoreUtil {

    private HotScoreUtil() {
    }

    /**
     * 时间衰减热度：互动总量 / (帖龄小时 + offset)^gravity
     */
    public static double decayScore(int likeCount, int favoriteCount, LocalDateTime createdAt,
                                    double gravity, double hourOffset, ZoneId zone) {
        if (createdAt == null) {
            return 0;
        }
        long millis = Duration.between(createdAt.atZone(zone).toInstant(), java.time.Instant.now()).toMillis();
        double hours = Math.max(0, millis / 3_600_000.0);
        double engagement = Math.max(0, (long) likeCount + (long) favoriteCount);
        return engagement / Math.pow(hours + hourOffset, gravity);
    }
}
