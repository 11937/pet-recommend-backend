package com.pet.community.service;

import com.pet.community.dto.CommentCreateDTO;
import com.pet.community.vo.CommunityCommentVO;
import com.pet.community.vo.PageSliceVO;

public interface CommunityCommentService {

    PageSliceVO<CommunityCommentVO> pageComments(long postId, long page, long size, Long viewerUserId);

    CommunityCommentVO addComment(long userId, long postId, CommentCreateDTO dto);
}
