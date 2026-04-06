package com.pet.user.controller;

import com.pet.common.entity.Result;
import com.pet.common.vo.PageSliceVO;
import com.pet.user.service.UserFollowService;
import com.pet.user.util.JwtBearerHelper;
import com.pet.user.vo.UserBriefVO;
import com.pet.user.vo.UserSocialStatsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 关注 / 粉丝。 */
@RestController
@RequestMapping("/user/social")
@RequiredArgsConstructor
public class UserSocialController {

    private final UserFollowService userFollowService;
    private final JwtBearerHelper jwtBearerHelper;

    @PostMapping("/follow/{followeeId}")
    public Result<Void> follow(@PathVariable long followeeId) {
        Long uid = jwtBearerHelper.getUserIdOrNull();
        if (uid == null) {
            return new Result<>(401, "未登录", null);
        }
        try {
            userFollowService.follow(uid, followeeId);
            return new Result<>("ok", null);
        } catch (IllegalArgumentException e) {
            return new Result<>(400, e.getMessage(), null);
        }
    }

    @DeleteMapping("/follow/{followeeId}")
    public Result<Void> unfollow(@PathVariable long followeeId) {
        Long uid = jwtBearerHelper.getUserIdOrNull();
        if (uid == null) {
            return new Result<>(401, "未登录", null);
        }
        userFollowService.unfollow(uid, followeeId);
        return new Result<>("ok", null);
    }

    @GetMapping("/follow/check/{userId}")
    public Result<Boolean> check(@PathVariable long userId) {
        Long uid = jwtBearerHelper.getUserIdOrNull();
        if (uid == null) {
            return new Result<>("ok", false);
        }
        return new Result<>("ok", userFollowService.isFollowing(uid, userId));
    }

    @GetMapping("/stats/{userId}")
    public Result<UserSocialStatsVO> stats(@PathVariable long userId) {
        UserSocialStatsVO vo = new UserSocialStatsVO();
        vo.setFollowerCount(userFollowService.countFollowers(userId));
        vo.setFollowingCount(userFollowService.countFollowing(userId));
        Long uid = jwtBearerHelper.getUserIdOrNull();
        if (uid == null) {
            vo.setFollowingByMe(null);
        } else {
            vo.setFollowingByMe(userFollowService.isFollowing(uid, userId));
        }
        return new Result<>("ok", vo);
    }

    @GetMapping("/followers/{userId}")
    public Result<PageSliceVO<UserBriefVO>> followers(
            @PathVariable long userId,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size) {
        return new Result<>("ok", userFollowService.pageFollowers(userId, page, size));
    }

    @GetMapping("/following/{userId}")
    public Result<PageSliceVO<UserBriefVO>> following(
            @PathVariable long userId,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size) {
        return new Result<>("ok", userFollowService.pageFollowing(userId, page, size));
    }
}
