package com.pet.community.web;

import com.pet.common.entity.Result;
import com.pet.community.service.CommunityPostService;
import com.pet.community.util.JwtRequestHelper;
import com.pet.community.vo.CommunityPostVO;
import com.pet.community.vo.PageSliceVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 帖子关键词搜索（公开帖）。 */
@RestController
@RequestMapping("/community/search")
@RequiredArgsConstructor
public class CommunitySearchController {

    private final CommunityPostService postService;
    private final JwtRequestHelper jwtRequestHelper;

    @GetMapping("/posts")
    public Result<PageSliceVO<CommunityPostVO>> searchPosts(
            @RequestParam("q") String q,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size) {
        Long uid = jwtRequestHelper.currentUserIdOrNull();
        return new Result<>("ok", postService.searchPosts(q, page, size, uid));
    }
}
