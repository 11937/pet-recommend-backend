package com.pet.user.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pet.common.util.JwtUtil;
import com.pet.user.dto.LoginDTO;
import com.pet.user.entity.User;
import com.pet.user.mapper.UserMapper;
import com.pet.user.service.UserService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private JwtUtil jwtUtil;

    // 密码加密工具
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public String login(LoginDTO dto) {
        // 1. 根据手机号查询用户
        User user = getOne(new LambdaQueryWrapper<User>()
                .eq(User::getPhone, dto.getPhone()));

        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 2. 校验密码
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        // 3. 生成JWT并返回
        return jwtUtil.generateToken(user.getId(), user.getPhone());
    }
}
