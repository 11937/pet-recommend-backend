package com.pet.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pet.common.entity.Result;
import com.pet.community.client.UserBriefInternalFeignClient;
import com.pet.community.client.dto.UserBriefJson;
import com.pet.community.dto.PostCreateDTO;
import com.pet.community.entity.CommunityAdminActionLog;
import com.pet.community.entity.CommunityPost;
import com.pet.community.entity.CommunityPostFavorite;
import com.pet.community.entity.CommunityPostLike;
import com.pet.community.entity.CommunityPostComment;
import com.pet.community.entity.CommunityPostMedia;
import com.pet.community.enums.AdminActionType;
import com.pet.community.enums.PostAuditStatus;
import com.pet.community.enums.PostMediaType;
import com.pet.community.enums.PostVisibilityStatus;
import com.pet.community.mapper.CommunityPostCommentMapper;
import com.pet.community.mapper.CommunityAdminActionLogMapper;
import com.pet.community.mapper.CommunityPostFavoriteMapper;
import com.pet.community.mapper.CommunityPostLikeMapper;
import com.pet.community.mapper.CommunityPostMapper;
import com.pet.community.mapper.CommunityPostMediaMapper;
import com.pet.community.service.CommunityAdminAuthService;
import com.pet.community.service.CommunityNotificationPublisher;
import com.pet.community.service.CommunityPostService;
import com.pet.community.util.PostDisplayTitleUtil;
import com.pet.community.vo.CommunityPostVO;
import com.pet.community.vo.PageSliceVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 帖子领域实现：媒体行表；点赞/收藏同步 interaction_score；管理员操作写审计表。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommunityPostServiceImpl implements CommunityPostService {

    private final CommunityPostMapper postMapper;
    private final CommunityPostMediaMapper mediaMapper;
    private final CommunityPostLikeMapper likeMapper;
    private final CommunityPostFavoriteMapper favoriteMapper;
    private final CommunityPostCommentMapper commentMapper;
    private final CommunityAdminActionLogMapper adminActionLogMapper;
    private final CommunityAdminAuthService adminAuthService;
    private final CommunityNotificationPublisher communityNotificationPublisher;
    private final UserBriefInternalFeignClient userBriefInternalFeignClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createPost(long authorUserId, PostCreateDTO dto) {
        CommunityPost post = new CommunityPost();
        post.setAuthorUserId(authorUserId);
        post.setContent(dto.getContent().trim());
        post.setLikeCount(0);
        post.setFavoriteCount(0);
        post.setInteractionScore(0L);
        post.setVisibilityStatus(PostVisibilityStatus.ONLINE.getCode());
        post.setAuditStatus(PostAuditStatus.APPROVED.getCode());
        postMapper.insert(post);

        List<String> images = dto.getImageUrls() == null ? Collections.emptyList() : dto.getImageUrls();
        int sort = 0;
        for (String url : images) {
            CommunityPostMedia m = new CommunityPostMedia();
            m.setPostId(post.getId());
            m.setMediaType(PostMediaType.IMAGE.getCode());
            m.setMediaUrl(url.trim());
            m.setSortNo(sort++);
            mediaMapper.insert(m);
        }
        List<String> videos = dto.getVideoUrls() == null ? Collections.emptyList() : dto.getVideoUrls();
        for (String url : videos) {
            CommunityPostMedia m = new CommunityPostMedia();
            m.setPostId(post.getId());
            m.setMediaType(PostMediaType.VIDEO.getCode());
            m.setMediaUrl(url.trim());
            m.setSortNo(sort++);
            mediaMapper.insert(m);
        }
        return post.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePostByAuthor(long authorUserId, long postId, PostCreateDTO dto) {
        CommunityPost post = postMapper.selectById(postId);
        if (post == null) {
            throw new IllegalArgumentException("帖子不存在");
        }
        if (!Objects.equals(post.getAuthorUserId(), authorUserId)) {
            throw new IllegalArgumentException("无权编辑该帖");
        }
        if (Objects.equals(post.getVisibilityStatus(), PostVisibilityStatus.ADMIN_OFFLINE.getCode())) {
            throw new IllegalArgumentException("管理员已下架，暂不可编辑");
        }
        CommunityPost update = new CommunityPost();
        update.setId(postId);
        update.setContent(dto.getContent().trim());
        postMapper.updateById(update);

        mediaMapper.delete(new LambdaQueryWrapper<CommunityPostMedia>()
                .eq(CommunityPostMedia::getPostId, postId));
        List<String> images = dto.getImageUrls() == null ? Collections.emptyList() : dto.getImageUrls();
        int sort = 0;
        for (String url : images) {
            CommunityPostMedia m = new CommunityPostMedia();
            m.setPostId(postId);
            m.setMediaType(PostMediaType.IMAGE.getCode());
            m.setMediaUrl(url.trim());
            m.setSortNo(sort++);
            mediaMapper.insert(m);
        }
        List<String> videos = dto.getVideoUrls() == null ? Collections.emptyList() : dto.getVideoUrls();
        for (String url : videos) {
            CommunityPostMedia m = new CommunityPostMedia();
            m.setPostId(postId);
            m.setMediaType(PostMediaType.VIDEO.getCode());
            m.setMediaUrl(url.trim());
            m.setSortNo(sort++);
            mediaMapper.insert(m);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void authorOffline(long authorUserId, long postId) {
        CommunityPost post = postMapper.selectById(postId);
        if (post == null) {
            throw new IllegalArgumentException("帖子不存在");
        }
        if (!Objects.equals(post.getAuthorUserId(), authorUserId)) {
            throw new IllegalArgumentException("无权操作该帖");
        }
        if (Objects.equals(post.getVisibilityStatus(), PostVisibilityStatus.ADMIN_OFFLINE.getCode())) {
            throw new IllegalArgumentException("管理员已下架，作者不可变更状态");
        }
        CommunityPost update = new CommunityPost();
        update.setId(postId);
        update.setVisibilityStatus(PostVisibilityStatus.AUTHOR_OFFLINE.getCode());
        update.setAuthorOfflineAt(LocalDateTime.now());
        postMapper.updateById(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adminOfflineByAdmin(long adminUserId, long postId, String reason) {
        CommunityPost post = postMapper.selectById(postId);
        if (post == null) {
            throw new IllegalArgumentException("帖子不存在");
        }
        Integer beforeVis = post.getVisibilityStatus();
        Integer beforeAudit = post.getAuditStatus();
        CommunityPost update = new CommunityPost();
        update.setId(postId);
        update.setVisibilityStatus(PostVisibilityStatus.ADMIN_OFFLINE.getCode());
        update.setAdminOfflineAt(LocalDateTime.now());
        update.setAdminOfflineReason(reason);
        postMapper.updateById(update);
        insertAdminLog(adminUserId, postId, AdminActionType.ADMIN_OFFLINE.getCode(), reason,
                beforeVis, PostVisibilityStatus.ADMIN_OFFLINE.getCode(), beforeAudit, beforeAudit);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adminSetAudit(long adminUserId, long postId, boolean approve, String remark) {
        CommunityPost post = postMapper.selectById(postId);
        if (post == null) {
            throw new IllegalArgumentException("帖子不存在");
        }
        Integer beforeVis = post.getVisibilityStatus();
        Integer beforeAudit = post.getAuditStatus();
        int afterAudit = approve ? PostAuditStatus.APPROVED.getCode() : PostAuditStatus.REJECTED.getCode();
        int action = approve ? AdminActionType.AUDIT_APPROVE.getCode() : AdminActionType.AUDIT_REJECT.getCode();

        CommunityPost update = new CommunityPost();
        update.setId(postId);
        update.setAuditStatus(afterAudit);
        postMapper.updateById(update);

        insertAdminLog(adminUserId, postId, action, remark,
                beforeVis, beforeVis, beforeAudit, afterAudit);
    }

    private void insertAdminLog(long adminUserId, long postId, int actionType, String reason,
                                Integer beforeVis, Integer afterVis, Integer beforeAudit, Integer afterAudit) {
        CommunityAdminActionLog log = new CommunityAdminActionLog();
        log.setAdminUserId(adminUserId);
        log.setTargetPostId(postId);
        log.setActionType(actionType);
        log.setActionReason(reason);
        log.setBeforeVisibilityStatus(beforeVis);
        log.setAfterVisibilityStatus(afterVis);
        log.setBeforeAuditStatus(beforeAudit);
        log.setAfterAuditStatus(afterAudit);
        adminActionLogMapper.insert(log);
    }

    @Override
    public CommunityPostVO getDetail(long postId, Long viewerUserId) {
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
        Boolean liked = null;
        Boolean favorited = null;
        if (viewerUserId != null) {
            liked = likeExists(postId, viewerUserId);
            favorited = favoriteExists(postId, viewerUserId);
        }
        Map<Long, MediaBucket> mediaMap = loadMediaMap(Collections.singletonList(postId));
        CommunityPostVO vo = toVo(post, viewerUserId, liked, favorited, mediaMap.get(postId));
        Integer cc = commentMapper.selectCount(new LambdaQueryWrapper<CommunityPostComment>()
                .eq(CommunityPostComment::getPostId, postId));
        vo.setCommentCount(cc == null ? 0L : cc.longValue());
        fillAuthorAndDisplayTitle(Collections.singletonList(vo));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean toggleLike(long userId, long postId) {
        assertInteractablePost(postId);
        LambdaQueryWrapper<CommunityPostLike> q = new LambdaQueryWrapper<CommunityPostLike>()
                .eq(CommunityPostLike::getPostId, postId)
                .eq(CommunityPostLike::getUserId, userId);
        CommunityPostLike row = likeMapper.selectOne(q);
        if (row != null) {
            likeMapper.deleteById(row.getId());
            postMapper.update(null, new LambdaUpdateWrapper<CommunityPost>()
                    .eq(CommunityPost::getId, postId)
                    .setSql("like_count = GREATEST(CAST(like_count AS SIGNED) - 1, 0), "
                            + "interaction_score = GREATEST(CAST(like_count AS SIGNED) - 1, 0) + favorite_count"));
            return false;
        }
        CommunityPostLike ins = new CommunityPostLike();
        ins.setPostId(postId);
        ins.setUserId(userId);
        likeMapper.insert(ins);
        postMapper.update(null, new LambdaUpdateWrapper<CommunityPost>()
                .eq(CommunityPost::getId, postId)
                .setSql("like_count = like_count + 1, interaction_score = like_count + favorite_count"));
        CommunityPost post = postMapper.selectById(postId);
        if (post != null && !Objects.equals(post.getAuthorUserId(), userId)) {
            communityNotificationPublisher.notifyLike(post.getAuthorUserId(), postId);
        }
        return true;
    }

    @Override
    public PageSliceVO<CommunityPostVO> searchPosts(String keyword, long page, long size, Long viewerUserId) {
        if (page < 1) {
            page = 1;
        }
        if (size < 1 || size > 50) {
            size = 20;
        }
        String q = keyword == null ? "" : keyword.trim();
        if (!StringUtils.hasText(q)) {
            PageSliceVO<CommunityPostVO> empty = new PageSliceVO<>();
            empty.setPage(page);
            empty.setSize(size);
            empty.setTotal(0);
            empty.setHasMore(false);
            return empty;
        }
        Page<CommunityPost> mp = postMapper.selectPage(
                new Page<>(page, size),
                publicFeedWrapper().like(CommunityPost::getContent, q).orderByDesc(CommunityPost::getCreatedAt));
        PageSliceVO<CommunityPostVO> vo = new PageSliceVO<>();
        vo.setPage(mp.getCurrent());
        vo.setSize(mp.getSize());
        vo.setTotal(mp.getTotal());
        vo.setHasMore(mp.getCurrent() * mp.getSize() < mp.getTotal());
        vo.setRecords(toVoList(mp.getRecords(), viewerUserId));
        return vo;
    }

    private LambdaQueryWrapper<CommunityPost> publicFeedWrapper() {
        return new LambdaQueryWrapper<CommunityPost>()
                .eq(CommunityPost::getVisibilityStatus, PostVisibilityStatus.ONLINE.getCode())
                .eq(CommunityPost::getAuditStatus, PostAuditStatus.APPROVED.getCode());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean toggleFavorite(long userId, long postId) {
        assertInteractablePost(postId);
        LambdaQueryWrapper<CommunityPostFavorite> q = new LambdaQueryWrapper<CommunityPostFavorite>()
                .eq(CommunityPostFavorite::getPostId, postId)
                .eq(CommunityPostFavorite::getUserId, userId);
        CommunityPostFavorite row = favoriteMapper.selectOne(q);
        if (row != null) {
            favoriteMapper.deleteById(row.getId());
            postMapper.update(null, new LambdaUpdateWrapper<CommunityPost>()
                    .eq(CommunityPost::getId, postId)
                    .setSql("favorite_count = GREATEST(CAST(favorite_count AS SIGNED) - 1, 0), "
                            + "interaction_score = like_count + GREATEST(CAST(favorite_count AS SIGNED) - 1, 0)"));
            return false;
        }
        CommunityPostFavorite ins = new CommunityPostFavorite();
        ins.setPostId(postId);
        ins.setUserId(userId);
        favoriteMapper.insert(ins);
        postMapper.update(null, new LambdaUpdateWrapper<CommunityPost>()
                .eq(CommunityPost::getId, postId)
                .setSql("favorite_count = favorite_count + 1, interaction_score = like_count + favorite_count"));
        return true;
    }

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

    private boolean likeExists(long postId, long userId) {
        Integer c = likeMapper.selectCount(new LambdaQueryWrapper<CommunityPostLike>()
                .eq(CommunityPostLike::getPostId, postId)
                .eq(CommunityPostLike::getUserId, userId));
        return c != null && c > 0;
    }

    private boolean favoriteExists(long postId, long userId) {
        Integer c = favoriteMapper.selectCount(new LambdaQueryWrapper<CommunityPostFavorite>()
                .eq(CommunityPostFavorite::getPostId, postId)
                .eq(CommunityPostFavorite::getUserId, userId));
        return c != null && c > 0;
    }

    @Override
    public List<CommunityPostVO> toVoList(List<CommunityPost> posts, Long viewerUserId) {
        if (posts == null || posts.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> ids = posts.stream().map(CommunityPost::getId).collect(Collectors.toList());
        Map<Long, Boolean> liked = loadLikeFlags(viewerUserId, ids);
        Map<Long, Boolean> fav = loadFavoriteFlags(viewerUserId, ids);
        Map<Long, MediaBucket> mediaMap = loadMediaMap(ids);
        List<CommunityPostVO> list = posts.stream()
                .map(p -> toVo(p, viewerUserId, liked.get(p.getId()), fav.get(p.getId()), mediaMap.get(p.getId())))
                .collect(Collectors.toList());
        fillAuthorAndDisplayTitle(list);
        return list;
    }

    @Override
    public CommunityPostVO toVo(CommunityPost post, Long viewerUserId, Boolean liked, Boolean favorited) {
        Map<Long, MediaBucket> mediaMap = loadMediaMap(Collections.singletonList(post.getId()));
        CommunityPostVO vo = toVo(post, viewerUserId, liked, favorited, mediaMap.get(post.getId()));
        fillAuthorAndDisplayTitle(Collections.singletonList(vo));
        return vo;
    }

    private CommunityPostVO toVo(CommunityPost post, Long viewerUserId, Boolean liked, Boolean favorited, MediaBucket media) {
        CommunityPostVO vo = new CommunityPostVO();
        vo.setId(post.getId());
        vo.setAuthorUserId(post.getAuthorUserId());
        vo.setContent(post.getContent());
        if (media != null) {
            vo.setImageUrls(new ArrayList<>(media.images));
            vo.setVideoUrls(new ArrayList<>(media.videos));
        } else {
            vo.setImageUrls(new ArrayList<>());
            vo.setVideoUrls(new ArrayList<>());
        }
        vo.setLikeCount(post.getLikeCount());
        vo.setFavoriteCount(post.getFavoriteCount());
        vo.setCommentCount(null);
        vo.setEngagementTotal(post.getInteractionScore());
        vo.setVisibilityStatus(post.getVisibilityStatus());
        vo.setAuditStatus(post.getAuditStatus());
        vo.setCreatedAt(post.getCreatedAt());
        if (viewerUserId == null) {
            vo.setLikedByMe(false);
            vo.setFavoritedByMe(false);
        } else {
            vo.setLikedByMe(Boolean.TRUE.equals(liked));
            vo.setFavoritedByMe(Boolean.TRUE.equals(favorited));
        }
        return vo;
    }

    /** 填充列表标题与作者信息（用户服务失败时仅标题仍可用）。 */
    private void fillAuthorAndDisplayTitle(List<CommunityPostVO> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        for (CommunityPostVO vo : list) {
            vo.setDisplayTitle(PostDisplayTitleUtil.fromContent(vo.getContent()));
        }
        List<Long> authorIds = list.stream()
                .map(CommunityPostVO::getAuthorUserId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (authorIds.isEmpty()) {
            return;
        }
        try {
            Result<List<UserBriefJson>> res = userBriefInternalFeignClient.usersBrief(authorIds);
            if (res == null || res.getCode() != 200 || res.getData() == null) {
                return;
            }
            Map<Long, UserBriefJson> byId = res.getData().stream()
                    .filter(u -> u.getId() != null)
                    .collect(Collectors.toMap(UserBriefJson::getId, u -> u, (a, b) -> a));
            for (CommunityPostVO vo : list) {
                UserBriefJson u = byId.get(vo.getAuthorUserId());
                if (u != null) {
                    vo.setAuthorNickName(u.getNickName());
                    vo.setAuthorUsername(u.getUsername());
                    vo.setAuthorAvatar(u.getAvatar());
                }
            }
        } catch (Exception e) {
            log.warn("批量拉取作者信息失败: {}", e.getMessage());
        }
    }

    private Map<Long, MediaBucket> loadMediaMap(List<Long> postIds) {
        if (postIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<CommunityPostMedia> rows = mediaMapper.selectList(new LambdaQueryWrapper<CommunityPostMedia>()
                .in(CommunityPostMedia::getPostId, postIds)
                .orderByAsc(CommunityPostMedia::getPostId)
                .orderByAsc(CommunityPostMedia::getSortNo));
        Map<Long, MediaBucket> map = new LinkedHashMap<>();
        for (Long id : postIds) {
            map.put(id, new MediaBucket());
        }
        for (CommunityPostMedia row : rows) {
            MediaBucket b = map.computeIfAbsent(row.getPostId(), k -> new MediaBucket());
            if (Objects.equals(row.getMediaType(), PostMediaType.IMAGE.getCode())) {
                b.images.add(row.getMediaUrl());
            } else if (Objects.equals(row.getMediaType(), PostMediaType.VIDEO.getCode())) {
                b.videos.add(row.getMediaUrl());
            }
        }
        return map;
    }

    private Map<Long, Boolean> loadLikeFlags(Long viewerUserId, List<Long> postIds) {
        if (viewerUserId == null || postIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<CommunityPostLike> rows = likeMapper.selectList(new LambdaQueryWrapper<CommunityPostLike>()
                .eq(CommunityPostLike::getUserId, viewerUserId)
                .in(CommunityPostLike::getPostId, postIds));
        Set<Long> set = rows.stream().map(CommunityPostLike::getPostId).collect(Collectors.toSet());
        Map<Long, Boolean> map = new HashMap<>();
        for (Long id : postIds) {
            map.put(id, set.contains(id));
        }
        return map;
    }

    private Map<Long, Boolean> loadFavoriteFlags(Long viewerUserId, List<Long> postIds) {
        if (viewerUserId == null || postIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<CommunityPostFavorite> rows = favoriteMapper.selectList(new LambdaQueryWrapper<CommunityPostFavorite>()
                .eq(CommunityPostFavorite::getUserId, viewerUserId)
                .in(CommunityPostFavorite::getPostId, postIds));
        Set<Long> set = rows.stream().map(CommunityPostFavorite::getPostId).collect(Collectors.toSet());
        Map<Long, Boolean> map = new HashMap<>();
        for (Long id : postIds) {
            map.put(id, set.contains(id));
        }
        return map;
    }

    private static class MediaBucket {
        private final List<String> images = new ArrayList<>();
        private final List<String> videos = new ArrayList<>();
    }
}
