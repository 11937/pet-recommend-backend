package com.pet.community.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("community_admin_action_log")
public class CommunityAdminActionLog {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long adminUserId;
    private Long targetPostId;
    private Integer actionType;
    private String actionReason;
    private Integer beforeVisibilityStatus;
    private Integer afterVisibilityStatus;
    private Integer beforeAuditStatus;
    private Integer afterAuditStatus;
    private LocalDateTime createdAt;
}
