package com.pet.user.service;

import com.pet.common.dto.NotificationPushDTO;
import com.pet.common.vo.PageSliceVO;
import com.pet.user.vo.UserNotificationVO;

public interface UserNotificationService {

    void push(NotificationPushDTO dto);

    PageSliceVO<UserNotificationVO> page(long userId, long page, long size);

    long unreadCount(long userId);

    void markRead(long userId, long notificationId);

    void markAllRead(long userId);
}
