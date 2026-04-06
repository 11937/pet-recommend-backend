package com.pet.user.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pet.common.dto.NotificationPushDTO;
import com.pet.common.vo.PageSliceVO;
import com.pet.user.entity.User;
import com.pet.user.entity.UserFollow;
import com.pet.user.mapper.UserFollowMapper;
import com.pet.user.mapper.UserMapper;
import com.pet.user.service.UserFollowService;
import com.pet.user.service.UserNotificationService;
import com.pet.user.vo.UserBriefVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserFollowServiceImpl implements UserFollowService {

    private static final int MAX_FOLLOWING_IDS = 500;

    private final UserFollowMapper followMapper;
    private final UserMapper userMapper;
    private final UserNotificationService notificationService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void follow(long followerId, long followeeId) {
        if (followerId == followeeId) {
            throw new IllegalArgumentException("不能关注自己");
        }
        User target = userMapper.selectById(followeeId);
        if (target == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        LambdaQueryWrapper<UserFollow> q = new LambdaQueryWrapper<UserFollow>()
                .eq(UserFollow::getFollowerId, followerId)
                .eq(UserFollow::getFolloweeId, followeeId);
        if (followMapper.selectOne(q) != null) {
            return;
        }
        UserFollow row = new UserFollow();
        row.setFollowerId(followerId);
        row.setFolloweeId(followeeId);
        followMapper.insert(row);

        User actor = userMapper.selectById(followerId);
        String name = displayName(actor, followerId);
        NotificationPushDTO dto = new NotificationPushDTO();
        dto.setUserId(followeeId);
        dto.setType("FOLLOW");
        dto.setTitle("新粉丝");
        dto.setBody(name + " 关注了你");
        dto.setRefType("USER");
        dto.setRefId(followerId);
        notificationService.push(dto);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unfollow(long followerId, long followeeId) {
        followMapper.delete(new LambdaQueryWrapper<UserFollow>()
                .eq(UserFollow::getFollowerId, followerId)
                .eq(UserFollow::getFolloweeId, followeeId));
    }

    @Override
    public boolean isFollowing(long followerId, long followeeId) {
        Integer c = followMapper.selectCount(new LambdaQueryWrapper<UserFollow>()
                .eq(UserFollow::getFollowerId, followerId)
                .eq(UserFollow::getFolloweeId, followeeId));
        return c != null && c > 0;
    }

    @Override
    public long countFollowers(long userId) {
        Integer c = followMapper.selectCount(new LambdaQueryWrapper<UserFollow>()
                .eq(UserFollow::getFolloweeId, userId));
        return c == null ? 0L : c.longValue();
    }

    @Override
    public long countFollowing(long userId) {
        Integer c = followMapper.selectCount(new LambdaQueryWrapper<UserFollow>()
                .eq(UserFollow::getFollowerId, userId));
        return c == null ? 0L : c.longValue();
    }

    @Override
    public PageSliceVO<UserBriefVO> pageFollowers(long userId, long page, long size) {
        if (page < 1) {
            page = 1;
        }
        if (size < 1 || size > 50) {
            size = 20;
        }
        Page<UserFollow> mp = followMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<UserFollow>()
                        .eq(UserFollow::getFolloweeId, userId)
                        .orderByDesc(UserFollow::getCreatedAt));
        return toUserBriefSlice(mp, UserFollow::getFollowerId);
    }

    @Override
    public PageSliceVO<UserBriefVO> pageFollowing(long userId, long page, long size) {
        if (page < 1) {
            page = 1;
        }
        if (size < 1 || size > 50) {
            size = 20;
        }
        Page<UserFollow> mp = followMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<UserFollow>()
                        .eq(UserFollow::getFollowerId, userId)
                        .orderByDesc(UserFollow::getCreatedAt));
        return toUserBriefSlice(mp, UserFollow::getFolloweeId);
    }

    private PageSliceVO<UserBriefVO> toUserBriefSlice(Page<UserFollow> mp, java.util.function.Function<UserFollow, Long> idGetter) {
        List<Long> ids = mp.getRecords().stream().map(idGetter).collect(Collectors.toList());
        Map<Long, User> users = loadUsers(ids);
        PageSliceVO<UserBriefVO> vo = new PageSliceVO<>();
        vo.setPage(mp.getCurrent());
        vo.setSize(mp.getSize());
        vo.setTotal(mp.getTotal());
        vo.setHasMore(mp.getCurrent() * mp.getSize() < mp.getTotal());
        vo.setRecords(mp.getRecords().stream()
                .map(f -> toBrief(users.get(idGetter.apply(f))))
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
        return vo;
    }

    private Map<Long, User> loadUsers(List<Long> ids) {
        if (ids.isEmpty()) {
            return Collections.emptyMap();
        }
        return userMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));
    }

    private UserBriefVO toBrief(User u) {
        if (u == null) {
            return null;
        }
        UserBriefVO vo = new UserBriefVO();
        vo.setId(u.getId());
        vo.setUsername(u.getUsername());
        vo.setNickName(u.getNickName());
        vo.setAvatar(u.getAvatar());
        return vo;
    }

    private String displayName(User u, long fallbackId) {
        if (u != null && StringUtils.hasText(u.getNickName())) {
            return u.getNickName();
        }
        if (u != null && StringUtils.hasText(u.getUsername())) {
            return u.getUsername();
        }
        return "用户" + fallbackId;
    }

    @Override
    public List<Long> listFollowingIds(long userId, int max) {
        int limit = Math.min(Math.max(max, 1), MAX_FOLLOWING_IDS);
        List<UserFollow> rows = followMapper.selectList(
                new LambdaQueryWrapper<UserFollow>()
                        .eq(UserFollow::getFollowerId, userId)
                        .orderByDesc(UserFollow::getCreatedAt)
                        .last("LIMIT " + limit));
        return rows.stream().map(UserFollow::getFolloweeId).collect(Collectors.toList());
    }
}
