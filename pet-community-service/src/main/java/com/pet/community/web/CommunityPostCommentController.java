package com.pet.community.web;

import com.pet.common.entity.Result;
import com.pet.community.dto.CommentCreateDTO;
import com.pet.community.service.CommunityCommentService;
import com.pet.community.util.JwtRequestHelper;
import com.pet.community.vo.CommunityCommentVO;
import com.pet.community.vo.PageSliceVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/community/posts")
@RequiredArgsConstructor
public class CommunityPostCommentController {

    private final CommunityCommentService commentService;
    private final JwtRequestHelper jwtRequestHelper;

    @GetMapping("/{id}/comments")
    public Result<PageSliceVO<CommunityCommentVO>> list(
            @PathVariable("id") long id,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size) {
        Long viewer = jwtRequestHelper.currentUserIdOrNull();
        return new Result<>("ok", commentService.pageComments(id, page, size, viewer));
    }

    @PostMapping("/{id}/comments")
    public Result<CommunityCommentVO> add(
            @PathVariable("id") long id,
            @Valid @RequestBody CommentCreateDTO dto) {
        long uid = jwtRequestHelper.requireUserId();
        return new Result<>("ok", commentService.addComment(uid, id, dto));
    }
}
