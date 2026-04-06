package com.pet.common.dto;

import lombok.Data;

/** 社区等内部服务向用户服务推送通知的载荷。 */
@Data
public class NotificationPushDTO {
    private Long userId;
    private String type;
    private String title;
    private String body;
    private String refType;
    private Long refId;
}
