package com.pet.community.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("community_post_media")
public class CommunityPostMedia {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long postId;
    /** 1 图片 2 视频 */
    private Integer mediaType;
    private String mediaUrl;
    private Integer sortNo;
    private LocalDateTime createdAt;
}
