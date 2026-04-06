package com.pet.user.service;

import com.pet.common.vo.PageSliceVO;
import com.pet.user.vo.UserBriefVO;

import java.util.List;

public interface UserFollowService {

    void follow(long followerId, long followeeId);

    void unfollow(long followerId, long followeeId);

    boolean isFollowing(long followerId, long followeeId);

    long countFollowers(long userId);

    long countFollowing(long userId);

    PageSliceVO<UserBriefVO> pageFollowers(long userId, long page, long size);

    PageSliceVO<UserBriefVO> pageFollowing(long userId, long page, long size);

    /** 关注的人的 id 列表，按时间倒序，最多 max 条（供社区关注流）。 */
    List<Long> listFollowingIds(long userId, int max);
}
