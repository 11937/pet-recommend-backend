package com.pet.community.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** 帖子对外展示结构；videoUrls 暂恒为空列表。 */
@Data
public class CommunityPostVO {

    private Long id;
    private Long authorUserId;
    /** 列表/详情展示：从正文推导的标题 */
    private String displayTitle;
    /** 作者昵称（用户服务填充） */
    private String authorNickName;
    /** 作者登录名（无昵称时前端可展示） */
    private String authorUsername;
    /** 作者头像 URL */
    private String authorAvatar;
    private String content;
    private List<String> imageUrls = new ArrayList<>();
    /** 视频 URL 列表，来自 media 表 type=视频 */
    private List<String> videoUrls = new ArrayList<>();
    private Integer likeCount;
    private Integer favoriteCount;
    /** 评论条数（详情接口填充；列表可为 null） */
    private Long commentCount;
    /** 与库 interaction_score 一致（赞+藏冗余） */
    private Long engagementTotal;
    private Integer visibilityStatus;
    private Integer auditStatus;
    private LocalDateTime createdAt;
    private Boolean likedByMe;
    private Boolean favoritedByMe;
}
