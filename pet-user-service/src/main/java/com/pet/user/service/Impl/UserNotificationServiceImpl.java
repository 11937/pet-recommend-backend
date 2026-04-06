package com.pet.user.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pet.common.dto.NotificationPushDTO;
import com.pet.common.vo.PageSliceVO;
import com.pet.user.entity.UserNotification;
import com.pet.user.mapper.UserNotificationMapper;
import com.pet.user.service.UserNotificationService;
import com.pet.user.vo.UserNotificationVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserNotificationServiceImpl implements UserNotificationService {

    private final UserNotificationMapper notificationMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void push(NotificationPushDTO dto) {
        if (dto == null || dto.getUserId() == null) {
            return;
        }
        UserNotification n = new UserNotification();
        n.setUserId(dto.getUserId());
        n.setType(dto.getType() != null ? dto.getType() : "SYSTEM");
        n.setTitle(dto.getTitle() != null ? dto.getTitle() : "");
        n.setBody(dto.getBody());
        n.setRefType(dto.getRefType());
        n.setRefId(dto.getRefId());
        n.setCreatedAt(LocalDateTime.now());
        notificationMapper.insert(n);
    }

    @Override
    public PageSliceVO<UserNotificationVO> page(long userId, long page, long size) {
        if (page < 1) {
            page = 1;
        }
        if (size < 1 || size > 50) {
            size = 20;
        }
        Page<UserNotification> mp = notificationMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<UserNotification>()
                        .eq(UserNotification::getUserId, userId)
                        .orderByDesc(UserNotification::getCreatedAt));
        PageSliceVO<UserNotificationVO> vo = new PageSliceVO<>();
        vo.setPage(mp.getCurrent());
        vo.setSize(mp.getSize());
        vo.setTotal(mp.getTotal());
        vo.setHasMore(mp.getCurrent() * mp.getSize() < mp.getTotal());
        vo.setRecords(mp.getRecords().stream().map(this::toVo).collect(Collectors.toList()));
        return vo;
    }

    @Override
    public long unreadCount(long userId) {
        Integer c = notificationMapper.selectCount(new LambdaQueryWrapper<UserNotification>()
                .eq(UserNotification::getUserId, userId)
                .isNull(UserNotification::getReadAt));
        return c == null ? 0L : c.longValue();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markRead(long userId, long notificationId) {
        notificationMapper.update(null, new LambdaUpdateWrapper<UserNotification>()
                .eq(UserNotification::getId, notificationId)
                .eq(UserNotification::getUserId, userId)
                .isNull(UserNotification::getReadAt)
                .set(UserNotification::getReadAt, LocalDateTime.now()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAllRead(long userId) {
        notificationMapper.update(null, new LambdaUpdateWrapper<UserNotification>()
                .eq(UserNotification::getUserId, userId)
                .isNull(UserNotification::getReadAt)
                .set(UserNotification::getReadAt, LocalDateTime.now()));
    }

    private UserNotificationVO toVo(UserNotification n) {
        UserNotificationVO vo = new UserNotificationVO();
        vo.setId(n.getId());
        vo.setType(n.getType());
        vo.setTitle(n.getTitle());
        vo.setBody(n.getBody());
        vo.setRefType(n.getRefType());
        vo.setRefId(n.getRefId());
        vo.setReadAt(n.getReadAt());
        vo.setCreatedAt(n.getCreatedAt());
        return vo;
    }
}
