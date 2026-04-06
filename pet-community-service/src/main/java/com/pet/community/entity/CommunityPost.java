package com.pet.community.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 帖子主表；图片/视频在 community_post_media。 */
@Data
@TableName("community_post")
public class CommunityPost {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long authorUserId;
    private String content;
    private Integer likeCount;
    private Integer favoriteCount;
    /** 冗余：点赞+收藏，供日榜与排序 */
    private Long interactionScore;
    private Integer visibilityStatus;
    private Integer auditStatus;
    private LocalDateTime authorOfflineAt;
    private LocalDateTime adminOfflineAt;
    private String adminOfflineReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
