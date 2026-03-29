package com.pet.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pet.user.dto.RegisterDTO;
import com.pet.user.entity.User;

public interface RegisterService extends IService<User> {
    String register(RegisterDTO dto);
}
