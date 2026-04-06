package com.pet.user.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserNotificationVO {
    private Long id;
    private String type;
    private String title;
    private String body;
    private String refType;
    private Long refId;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;
}
