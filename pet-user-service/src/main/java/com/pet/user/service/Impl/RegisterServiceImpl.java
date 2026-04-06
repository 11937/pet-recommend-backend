package com.pet.user.service.Impl;

import com.alibaba.nacos.common.utils.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pet.common.util.JwtUtil;
import com.pet.user.dto.RegisterDTO;
import com.pet.user.entity.User;
import com.pet.user.mapper.UserMapper;
import com.pet.user.service.RegisterService;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;

/** 注册：校验手机号唯一、密码加密、默认昵称与角色/VIP，成功后签发 JWT。 */
@Service
public class RegisterServiceImpl extends ServiceImpl<UserMapper, User> implements RegisterService {

    // 注入密码加密器
    @Resource
    private PasswordEncoder passwordEncoder;

    // 注入JWT工具
    @Resource
    private JwtUtil jwtUtil;

    @Override
    public String register(RegisterDTO dto) {
        // 1. 校验手机号是否已存在
        User exist = getOne(new LambdaQueryWrapper<User>()
                .eq(User::getPhone, dto.getPhone()));
        if (exist != null) {
            throw new RuntimeException("手机号已注册");
        }

        // 2. 创建新用户
        User user = new User();
        user.setPhone(dto.getPhone());
        // 密码加密
        user.setPassword(passwordEncoder.encode(dto.getPassword()));

        // 昵称处理：如果未传则生成默认昵称
        if (StringUtils.hasText(dto.getNickname())) {
            user.setNickname(dto.getNickname());
        } else {
            // 默认昵称：“用户”+手机号后四位
            String lastFour = dto.getPhone().substring(7);
            user.setNickname("用户" + lastFour);
        }

        // 设置默认头像（可根据需要配置）
        user.setAvatar("https://your-domain.com/default-avatar.png");
        user.setVipLevel(0);          // 普通会员
        user.setRole("USER");
        // vip_expire 默认为 null

        // 3. 保存到数据库
        save(user);

        // 4. 生成 JWT 并返回（注册后自动登录）
        return jwtUtil.generateToken(user.getId(), user.getPhone());
    }
}