package com.pet.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pet.user.dto.LoginDTO;
import com.pet.user.entity.User;

public interface UserService extends IService<User> {
    String login(LoginDTO dto);

}
