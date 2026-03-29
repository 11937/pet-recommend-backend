package com.pet.user.controller;

import com.pet.common.entity.Result;
import com.pet.user.dto.RegisterDTO;
import com.pet.user.service.RegisterService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.annotation.Resource;
import javax.validation.Valid;

@RestController
@RequestMapping("/user")
public class UserRegisterController {

    @Resource
    private RegisterService registerService;

    @PostMapping("/register")
    public Result<String> register(@Valid @RequestBody RegisterDTO dto) {
        String token = registerService.register(dto);
        return new Result("注册成功", token);
    }
}
