package com.pet.community.service;

import com.pet.community.dto.PostCreateDTO;
import com.pet.community.entity.CommunityPost;
import com.pet.community.vo.CommunityPostVO;
import com.pet.community.vo.PageSliceVO;

import java.util.List;

/** 帖子 CRUD、下架、点赞/收藏、实体转 VO。 */
public interface CommunityPostService {

    Long createPost(long authorUserId, PostCreateDTO dto);

    /** 作者更新正文与媒体（与发帖相同校验）；管理员已下架的帖子不可编辑。 */
    void updatePostByAuthor(long authorUserId, long postId, PostCreateDTO dto);

    void authorOffline(long authorUserId, long postId);

    void adminOfflineByAdmin(long adminUserId, long postId, String reason);

    void adminSetAudit(long adminUserId, long postId, boolean approve, String remark);

    CommunityPostVO getDetail(long postId, Long viewerUserId);

    boolean toggleLike(long userId, long postId);

    boolean toggleFavorite(long userId, long postId);

    List<CommunityPostVO> toVoList(List<CommunityPost> posts, Long viewerUserId);

    CommunityPostVO toVo(CommunityPost post, Long viewerUserId, Boolean liked, Boolean favorited);

    /** 公开帖全文检索（内容 LIKE）。 */
    PageSliceVO<CommunityPostVO> searchPosts(String keyword, long page, long size, Long viewerUserId);
}
