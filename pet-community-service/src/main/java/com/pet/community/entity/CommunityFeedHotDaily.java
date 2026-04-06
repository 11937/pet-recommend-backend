package com.pet.community.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("community_feed_hot_daily")
public class CommunityFeedHotDaily {

    @TableId(type = IdType.AUTO)
    private Long id;
    private LocalDate statDate;
    private Integer rankNo;
    private Long postId;
    private Long interactionScore;
    private LocalDateTime createdAt;
}
