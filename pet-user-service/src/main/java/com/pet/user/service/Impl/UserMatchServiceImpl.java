package com.pet.user.service.Impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pet.user.entity.UserMatch;
import com.pet.user.mapper.UserMatchMapper;
import com.pet.user.service.UserMatchService;
import org.springframework.stereotype.Service;

@Service
public class UserMatchServiceImpl extends ServiceImpl<UserMatchMapper, UserMatch> implements UserMatchService {
}
