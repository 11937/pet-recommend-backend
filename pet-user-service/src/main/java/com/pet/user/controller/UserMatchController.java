package com.pet.user.controller;


import com.pet.common.entity.Result;
import com.pet.user.dto.UserMatchSaveDTO;
import com.pet.user.entity.UserMatch;
import com.pet.user.service.UserMatchService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/user/match")
public class UserMatchController {

    @Resource
    private UserMatchService userMatchService;

    @PostMapping
    public Result<Void> saveMatch(@RequestBody UserMatchSaveDTO dto,
                                  @RequestHeader("X-User-Id") Long userId) {
        UserMatch match = new UserMatch();
        match.setUserId(userId);
        match.setMatchParams(dto.getMatchParams());
        match.setResult(dto.getResult());
        userMatchService.save(match);
        return new Result(200,"成功");
    }
}