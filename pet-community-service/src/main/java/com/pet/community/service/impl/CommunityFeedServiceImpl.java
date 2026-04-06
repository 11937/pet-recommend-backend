package com.pet.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pet.common.entity.Result;
import com.pet.community.client.UserSocialInternalFeignClient;
import com.pet.community.config.CommunityHotProperties;
import com.pet.community.entity.CommunityFeedTopSlotsDaily;
import com.pet.community.entity.CommunityPost;
import com.pet.community.enums.PostAuditStatus;
import com.pet.community.enums.PostVisibilityStatus;
import com.pet.community.mapper.CommunityFeedTopSlotsDailyMapper;
import com.pet.community.mapper.CommunityPostMapper;
import com.pet.community.service.CommunityFeedService;
import com.pet.community.service.CommunityPostService;
import com.pet.community.util.HotScoreUtil;
import com.pet.community.vo.CommunityPostVO;
import com.pet.community.vo.PageSliceVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 最新列表；热度 = 当日顶部 2 卡位 + 时间衰减流（排除卡位帖）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommunityFeedServiceImpl implements CommunityFeedService {

    private static final ZoneId SHANGHAI = ZoneId.of("Asia/Shanghai");

    private final CommunityPostMapper postMapper;
    private final CommunityFeedTopSlotsDailyMapper topSlotsMapper;
    private final CommunityPostService postService;
    private final CommunityHotProperties hotProperties;
    private final UserSocialInternalFeignClient userSocialInternalFeignClient;

    @Override
    public PageSliceVO<CommunityPostVO> pageLatest(long page, long size, Long viewerUserId) {
        if (page < 1) {
            page = 1;
        }
        if (size < 1 || size > 50) {
            size = 20;
        }
        LambdaQueryWrapper<CommunityPost> q = publicFeedWrapper().orderByDesc(CommunityPost::getCreatedAt);
        Page<CommunityPost> mp = postMapper.selectPage(new Page<>(page, size), q);
        PageSliceVO<CommunityPostVO> vo = new PageSliceVO<>();
        vo.setPage(page);
        vo.setSize(size);
        vo.setTotal(mp.getTotal());
        vo.setHasMore(mp.getCurrent() * mp.getSize() < mp.getTotal());
        vo.setRecords(postService.toVoList(mp.getRecords(), viewerUserId));
        return vo;
    }

    @Override
    public PageSliceVO<CommunityPostVO> pageHotRotated(long page, long size, Long viewerUserId) {
        // 当日置顶卡位 + 排除置顶后的衰减分排序，再合并分页
        if (page < 1) {
            page = 1;
        }
        if (size < 1 || size > 50) {
            size = 20;
        }

        LocalDate today = LocalDate.now(SHANGHAI);
        List<CommunityFeedTopSlotsDaily> slotRows = topSlotsMapper.selectList(
                new LambdaQueryWrapper<CommunityFeedTopSlotsDaily>()
                        .eq(CommunityFeedTopSlotsDaily::getSlotDate, today)
                        .orderByAsc(CommunityFeedTopSlotsDaily::getSlotNo));

        List<Long> pinnedIds = slotRows.stream()
                .map(CommunityFeedTopSlotsDaily::getPostId)
                .collect(Collectors.toList());

        List<CommunityPost> pinnedPosts = new ArrayList<>();
        if (!pinnedIds.isEmpty()) {
            List<CommunityPost> batch = postMapper.selectBatchIds(pinnedIds);
            Map<Long, CommunityPost> byId = batch.stream()
                    .collect(Collectors.toMap(CommunityPost::getId, p -> p, (a, b) -> a));
            for (Long pid : pinnedIds) {
                CommunityPost p = byId.get(pid);
                if (p != null && isPublicOnFeed(p)) {
                    pinnedPosts.add(p);
                }
            }
        }

        int p = pinnedPosts.size();
        int cap = Math.max(1, hotProperties.getCandidateLimit());

        LambdaQueryWrapper<CommunityPost> decayQw = publicFeedWrapper().orderByDesc(CommunityPost::getCreatedAt);
        if (!pinnedIds.isEmpty()) {
            decayQw.notIn(CommunityPost::getId, pinnedIds);
        }
        List<CommunityPost> candidates = postMapper.selectList(decayQw.last("LIMIT " + cap));

        double g = hotProperties.getDecayGravity();
        double off = hotProperties.getDecayHourOffset();
        List<CommunityPost> decaySorted = candidates.stream()
                .sorted(Comparator.comparingDouble(
                        (CommunityPost po) -> HotScoreUtil.decayScore(
                                po.getLikeCount(),
                                po.getFavoriteCount(),
                                po.getCreatedAt(),
                                g,
                                off,
                                SHANGHAI)).reversed())
                .collect(Collectors.toList());

        long decayLen = decaySorted.size();
        long total = p + decayLen;
        int from = (int) ((page - 1) * size);
        int to = (int) (from + size);

        List<CommunityPost> window = new ArrayList<>();
        if (from < p) {
            for (int i = from; i < Math.min(to, p); i++) {
                window.add(pinnedPosts.get(i));
            }
        }
        int decayStart = Math.max(0, from - p);
        int decayEnd = Math.max(0, to - p);
        for (int j = decayStart; j < decayEnd && j < decaySorted.size(); j++) {
            window.add(decaySorted.get(j));
        }

        PageSliceVO<CommunityPostVO> slice = new PageSliceVO<>();
        slice.setPage(page);
        slice.setSize(size);
        slice.setTotal(total);
        slice.setHasMore((long) to < total);
        slice.setRecords(postService.toVoList(window, viewerUserId));
        return slice;
    }

    @Override
    public PageSliceVO<CommunityPostVO> pageMine(long authorUserId, long page, long size) {
        if (page < 1) {
            page = 1;
        }
        if (size < 1 || size > 50) {
            size = 20;
        }
        LambdaQueryWrapper<CommunityPost> q = new LambdaQueryWrapper<CommunityPost>()
                .eq(CommunityPost::getAuthorUserId, authorUserId)
                .orderByDesc(CommunityPost::getCreatedAt);
        Page<CommunityPost> mp = postMapper.selectPage(new Page<>(page, size), q);
        PageSliceVO<CommunityPostVO> vo = new PageSliceVO<>();
        vo.setPage(page);
        vo.setSize(size);
        vo.setTotal(mp.getTotal());
        vo.setHasMore(mp.getCurrent() * mp.getSize() < mp.getTotal());
        vo.setRecords(postService.toVoList(mp.getRecords(), authorUserId));
        return vo;
    }

    @Override
    public PageSliceVO<CommunityPostVO> pageFollowing(long viewerUserId, long page, long size) {
        if (page < 1) {
            page = 1;
        }
        if (size < 1 || size > 50) {
            size = 20;
        }
        List<Long> followingIds = Collections.emptyList();
        try {
            Result<List<Long>> r = userSocialInternalFeignClient.listFollowingIds(viewerUserId);
            if (r != null && r.getCode() == 200 && r.getData() != null) {
                followingIds = r.getData();
            }
        } catch (Exception e) {
            log.warn("拉取关注列表失败 userId={}", viewerUserId, e);
        }
        if (followingIds.isEmpty()) {
            PageSliceVO<CommunityPostVO> empty = new PageSliceVO<>();
            empty.setPage(page);
            empty.setSize(size);
            empty.setTotal(0);
            empty.setHasMore(false);
            return empty;
        }
        Page<CommunityPost> mp = postMapper.selectPage(
                new Page<>(page, size),
                publicFeedWrapper().in(CommunityPost::getAuthorUserId, followingIds)
                        .orderByDesc(CommunityPost::getCreatedAt));
        PageSliceVO<CommunityPostVO> vo = new PageSliceVO<>();
        vo.setPage(mp.getCurrent());
        vo.setSize(mp.getSize());
        vo.setTotal(mp.getTotal());
        vo.setHasMore(mp.getCurrent() * mp.getSize() < mp.getTotal());
        vo.setRecords(postService.toVoList(mp.getRecords(), viewerUserId));
        return vo;
    }

    private LambdaQueryWrapper<CommunityPost> publicFeedWrapper() {
        return new LambdaQueryWrapper<CommunityPost>()
                .eq(CommunityPost::getVisibilityStatus, PostVisibilityStatus.ONLINE.getCode())
                .eq(CommunityPost::getAuditStatus, PostAuditStatus.APPROVED.getCode());
    }

    private boolean isPublicOnFeed(CommunityPost post) {
        return Objects.equals(post.getVisibilityStatus(), PostVisibilityStatus.ONLINE.getCode())
                && Objects.equals(post.getAuditStatus(), PostAuditStatus.APPROVED.getCode());
    }
}
