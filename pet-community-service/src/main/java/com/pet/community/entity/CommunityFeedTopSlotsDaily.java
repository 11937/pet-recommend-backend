package com.pet.community.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("community_feed_top_slots_daily")
public class CommunityFeedTopSlotsDaily {

    @TableId(type = IdType.AUTO)
    private Long id;
    private LocalDate slotDate;
    private Integer slotNo;
    private Long postId;
    private Integer sourceRank;
    private LocalDateTime createdAt;
}
