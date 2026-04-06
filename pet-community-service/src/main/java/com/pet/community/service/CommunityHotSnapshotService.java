package com.pet.community.service;

import java.time.LocalDate;

/**
 * 每日重建互动 Top100 与首页 2 卡位（上海日历日）。
 */
public interface CommunityHotSnapshotService {

    void buildSnapshotForShanghaiDate(LocalDate statDate);
}
