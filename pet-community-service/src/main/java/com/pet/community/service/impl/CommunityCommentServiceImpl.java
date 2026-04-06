package com.pet.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pet.community.dto.CommentCreateDTO;
import com.pet.community.entity.CommunityPost;
import com.pet.community.entity.CommunityPostComment;
import com.pet.community.enums.PostAuditStatus;
import com.pet.community.enums.PostVisibilityStatus;
import com.pet.community.mapper.CommunityPostCommentMapper;
import com.pet.community.mapper.CommunityPostMapper;
import com.pet.community.service.CommunityAdminAuthService;
import com.pet.community.service.CommunityCommentService;
import com.pet.community.service.CommunityNotificationPublisher;
import com.pet.community.vo.CommunityCommentVO;
import com.pet.community.vo.PageSliceVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommunityCommentServiceImpl implements CommunityCommentService {

    private final CommunityPostMapper postMapper;
    private final CommunityPostCommentMapper commentMapper;
    private final CommunityAdminAuthService adminAuthService;
    private final CommunityNotificationPublisher communityNotificationPublisher;

    @Override
    public PageSliceVO<CommunityCommentVO> pageComments(long postId, long page, long size, Long viewerUserId) {
        assertCanViewPost(postId, viewerUserId);
        if (page < 1) {
            page = 1;
        }
        if (size < 1 || size > 50) {
            size = 20;
        }
        Page<CommunityPostComment> mp = commentMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<CommunityPostComment>()
                        .eq(CommunityPostComment::getPostId, postId)
                        .orderByDesc(CommunityPostComment::getCreatedAt));
        PageSliceVO<CommunityCommentVO> vo = new PageSliceVO<>();
        vo.setPage(page);
        vo.setSize(size);
        vo.setTotal(mp.getTotal());
        vo.setHasMore(mp.getCurrent() * mp.getSize() < mp.getTotal());
        vo.setRecords(mp.getRecords().stream().map(this::toVo).collect(Collectors.toList()));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommunityCommentVO addComment(long userId, long postId, CommentCreateDTO dto) {
        assertInteractablePost(postId);
        CommunityPostComment row = new CommunityPostComment();
        row.setPostId(postId);
        row.setUserId(userId);
        row.setContent(dto.getContent().trim());
        commentMapper.insert(row);
        CommunityPost post = postMapper.selectById(postId);
        if (post != null && !Objects.equals(post.getAuthorUserId(), userId)) {
            communityNotificationPublisher.notifyComment(post.getAuthorUserId(), postId);
        }
        CommunityPostComment loaded = commentMapper.selectById(row.getId());
        return toVo(loaded != null ? loaded : row);
    }

    private CommunityCommentVO toVo(CommunityPostComment c) {
        CommunityCommentVO vo = new CommunityCommentVO();
        vo.setId(c.getId());
        vo.setUserId(c.getUserId());
        vo.setContent(c.getContent());
        vo.setCreatedAt(c.getCreatedAt());
        return vo;
    }

    /** 与帖子详情可见性一致：公开帖任何人；非公开仅作者或管理员。 */
    private void assertCanViewPost(long postId, Long viewerUserId) {
        CommunityPost post = postMapper.selectById(postId);
        if (post == null) {
            throw new IllegalArgumentException("帖子不存在");
        }
        boolean isAuthor = viewerUserId != null && Objects.equals(viewerUserId, post.getAuthorUserId());
        boolean isAdminUser = viewerUserId != null && adminAuthService.isAdmin(viewerUserId);
        boolean publicOk = Objects.equals(post.getVisibilityStatus(), PostVisibilityStatus.ONLINE.getCode())
                && Objects.equals(post.getAuditStatus(), PostAuditStatus.APPROVED.getCode());
        if (!publicOk && !isAuthor && !isAdminUser) {
            throw new IllegalArgumentException("帖子不可见");
        }
    }

    /** 与点赞相同：仅上架且审核通过可评论。 */
    private void assertInteractablePost(long postId) {
        CommunityPost post = postMapper.selectById(postId);
        if (post == null) {
            throw new IllegalArgumentException("帖子不存在");
        }
        if (!Objects.equals(post.getVisibilityStatus(), PostVisibilityStatus.ONLINE.getCode())) {
            throw new IllegalArgumentException("帖子已下架");
        }
        if (!Objects.equals(post.getAuditStatus(), PostAuditStatus.APPROVED.getCode())) {
            throw new IllegalArgumentException("帖子未通过审核");
        }
    }
}
