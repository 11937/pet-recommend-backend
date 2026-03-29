package com.pet.user.controller;

import com.pet.common.entity.Result;
import com.pet.user.dto.LoginDTO;
import com.pet.user.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    @PostMapping("/login")
    public Result login(@RequestBody LoginDTO dto) {
        String token = userService.login(dto);
        return new Result("登录成功", token);
    }
}