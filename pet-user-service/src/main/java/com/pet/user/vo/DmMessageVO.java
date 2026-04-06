package com.pet.user.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DmMessageVO {
    private Long id;
    private Long senderId;
    private Long receiverId;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
}
