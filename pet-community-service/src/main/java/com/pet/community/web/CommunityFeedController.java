package com.pet.community.web;

import com.pet.common.entity.Result;
import com.pet.community.service.CommunityFeedService;
import com.pet.community.util.JwtRequestHelper;
import com.pet.community.vo.CommunityPostVO;
import com.pet.community.vo.PageSliceVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 列表：最新 / 热度 / 我的。 */
@RestController
@RequestMapping("/community/feed")
@RequiredArgsConstructor
public class CommunityFeedController {

    private final CommunityFeedService feedService;
    private final JwtRequestHelper jwtRequestHelper;

    @GetMapping("/latest")
    public Result<PageSliceVO<CommunityPostVO>> latest(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size) {
        Long uid = jwtRequestHelper.currentUserIdOrNull();
        return new Result<>("ok", feedService.pageLatest(page, size, uid));
    }

    @GetMapping("/hot")
    public Result<PageSliceVO<CommunityPostVO>> hot(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size) {
        Long uid = jwtRequestHelper.currentUserIdOrNull();
        return new Result<>("ok", feedService.pageHotRotated(page, size, uid));
    }

    @GetMapping("/mine")
    public Result<PageSliceVO<CommunityPostVO>> mine(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size) {
        long uid = jwtRequestHelper.requireUserId();
        return new Result<>("ok", feedService.pageMine(uid, page, size));
    }

    /** 关注的人的帖子（须登录；未关注任何人时为空列表）。 */
    @GetMapping("/following")
    public Result<PageSliceVO<CommunityPostVO>> following(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size) {
        long uid = jwtRequestHelper.requireUserId();
        return new Result<>("ok", feedService.pageFollowing(uid, page, size));
    }
}
