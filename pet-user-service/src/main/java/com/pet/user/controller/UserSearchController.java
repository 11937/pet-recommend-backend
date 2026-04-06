package com.pet.user.controller;

import com.pet.common.entity.Result;
import com.pet.common.vo.PageSliceVO;
import com.pet.user.service.UserSearchService;
import com.pet.user.vo.UserBriefVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 用户昵称/用户名搜索（不含手机号）。 */
@RestController
@RequestMapping("/user/search")
@RequiredArgsConstructor
public class UserSearchController {

    private final UserSearchService userSearchService;

    @GetMapping("/users")
    public Result<PageSliceVO<UserBriefVO>> searchUsers(
            @RequestParam("q") String q,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size) {
        return new Result<>("ok", userSearchService.searchUsers(q, page, size));
    }
}
