package com.pet.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pet.community.entity.CommunityFeedHotDaily;
import com.pet.community.entity.CommunityFeedTopSlotsDaily;
import com.pet.community.entity.CommunityPost;
import com.pet.community.enums.PostAuditStatus;
import com.pet.community.enums.PostVisibilityStatus;
import com.pet.community.mapper.CommunityFeedHotDailyMapper;
import com.pet.community.mapper.CommunityFeedTopSlotsDailyMapper;
import com.pet.community.mapper.CommunityPostMapper;
import com.pet.community.service.CommunityHotSnapshotService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * 每日热度快照：按互动分取 Top100 落库；前 2 个 Feed 卡位按「日期」轮转，减轻固定霸榜。
 */
@Service
@RequiredArgsConstructor
public class CommunityHotSnapshotServiceImpl implements CommunityHotSnapshotService {

    private final CommunityPostMapper postMapper;
    private final CommunityFeedHotDailyMapper hotDailyMapper;
    private final CommunityFeedTopSlotsDailyMapper slotsMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void buildSnapshotForShanghaiDate(LocalDate statDate) {
        // 当日重建：先删再算，避免重复跑任务产生脏数据
        hotDailyMapper.delete(new LambdaQueryWrapper<CommunityFeedHotDaily>()
                .eq(CommunityFeedHotDaily::getStatDate, statDate));
        slotsMapper.delete(new LambdaQueryWrapper<CommunityFeedTopSlotsDaily>()
                .eq(CommunityFeedTopSlotsDaily::getSlotDate, statDate));

        List<CommunityPost> top = postMapper.selectList(
                publicFeedWrapper()
                        .orderByDesc(CommunityPost::getInteractionScore)
                        .orderByDesc(CommunityPost::getId)
                        .last("LIMIT 100"));

        int n = top.size();
        for (int i = 0; i < n; i++) {
            CommunityPost p = top.get(i);
            CommunityFeedHotDaily row = new CommunityFeedHotDaily();
            row.setStatDate(statDate);
            row.setRankNo(i + 1);
            row.setPostId(p.getId());
            row.setInteractionScore(p.getInteractionScore());
            hotDailyMapper.insert(row);
        }

        if (n == 0) {
            return;
        }

        // 用「纪元日」做确定性轮转，使两卡位每日变化又可复现
        long dayIndex = ChronoUnit.DAYS.between(LocalDate.of(1970, 1, 1), statDate);
        int mod = n >= 100 ? 100 : n;
        int i1 = Math.floorMod((int) (dayIndex * 2), mod);
        int i2 = Math.floorMod((int) (dayIndex * 2 + 1), mod);

        CommunityFeedTopSlotsDaily s1 = new CommunityFeedTopSlotsDaily();
        s1.setSlotDate(statDate);
        s1.setSlotNo(1);
        s1.setPostId(top.get(i1).getId());
        s1.setSourceRank(i1 + 1);
        slotsMapper.insert(s1);

        if (n >= 2) {
            CommunityFeedTopSlotsDaily s2 = new CommunityFeedTopSlotsDaily();
            s2.setSlotDate(statDate);
            s2.setSlotNo(2);
            s2.setPostId(top.get(i2).getId());
            s2.setSourceRank(i2 + 1);
            slotsMapper.insert(s2);
        }
    }

    private LambdaQueryWrapper<CommunityPost> publicFeedWrapper() {
        return new LambdaQueryWrapper<CommunityPost>()
                .eq(CommunityPost::getVisibilityStatus, PostVisibilityStatus.ONLINE.getCode())
                .eq(CommunityPost::getAuditStatus, PostAuditStatus.APPROVED.getCode());
    }
}
