package com.pet.community.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommunityCommentVO {

    private Long id;
    private Long userId;
    private String content;
    private LocalDateTime createdAt;
}
