package com.pet.community.service;

import com.pet.common.dto.NotificationPushDTO;
import com.pet.common.entity.Result;
import com.pet.community.client.UserSocialInternalFeignClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 将社区事件转换为用户侧通知；失败仅打日志，不影响主事务。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommunityNotificationPublisher {

    private final UserSocialInternalFeignClient userSocialInternalFeignClient;

    public void notifyComment(long postAuthorId, long postId) {
        if (postAuthorId <= 0) {
            return;
        }
        NotificationPushDTO dto = new NotificationPushDTO();
        dto.setUserId(postAuthorId);
        dto.setType("COMMENT");
        dto.setTitle("新评论");
        dto.setBody("你的帖子收到一条新评论");
        dto.setRefType("POST");
        dto.setRefId(postId);
        push(dto);
    }

    public void notifyLike(long postAuthorId, long postId) {
        if (postAuthorId <= 0) {
            return;
        }
        NotificationPushDTO dto = new NotificationPushDTO();
        dto.setUserId(postAuthorId);
        dto.setType("LIKE");
        dto.setTitle("新点赞");
        dto.setBody("你的帖子收到一个新点赞");
        dto.setRefType("POST");
        dto.setRefId(postId);
        push(dto);
    }

    private void push(NotificationPushDTO dto) {
        try {
            Result<Void> r = userSocialInternalFeignClient.pushNotification(dto);
            if (r == null || r.getCode() != 200) {
                log.warn("推送通知非成功: code={} msg={}", r != null ? r.getCode() : null,
                        r != null ? r.getMsg() : null);
            }
        } catch (Exception e) {
            log.warn("推送通知失败 userId={} type={}", dto.getUserId(), dto.getType(), e);
        }
    }
}
