package com.pet.community.service;

import com.pet.community.vo.CommunityPostVO;
import com.pet.community.vo.PageSliceVO;

/** 对外展示的列表：最新、热度（双榜轮换）、我的帖子。 */
public interface CommunityFeedService {

    /** 最新发布列表。 */
    PageSliceVO<CommunityPostVO> pageLatest(long page, long size, Long viewerUserId);

    /**
     * 热度：当日顶部 2 卡位（日榜任务写入）+ 时间衰减流（排除卡位帖），分页。
     */
    PageSliceVO<CommunityPostVO> pageHotRotated(long page, long size, Long viewerUserId);

    /** 当前用户全部帖子（含下架）。 */
    PageSliceVO<CommunityPostVO> pageMine(long authorUserId, long page, long size);

    /** 仅展示当前用户所关注作者发布的公开帖。 */
    PageSliceVO<CommunityPostVO> pageFollowing(long viewerUserId, long page, long size);
}
