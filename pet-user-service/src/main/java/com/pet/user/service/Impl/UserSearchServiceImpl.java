package com.pet.user.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pet.common.vo.PageSliceVO;
import com.pet.user.entity.User;
import com.pet.user.mapper.UserMapper;
import com.pet.user.service.UserSearchService;
import com.pet.user.vo.UserBriefVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserSearchServiceImpl implements UserSearchService {

    private final UserMapper userMapper;

    @Override
    public PageSliceVO<UserBriefVO> searchUsers(String keyword, long page, long size) {
        if (page < 1) {
            page = 1;
        }
        if (size < 1 || size > 50) {
            size = 20;
        }
        String q = keyword == null ? "" : keyword.trim();
        if (!StringUtils.hasText(q)) {
            PageSliceVO<UserBriefVO> empty = new PageSliceVO<>();
            empty.setPage(page);
            empty.setSize(size);
            empty.setTotal(0);
            empty.setHasMore(false);
            return empty;
        }
        Page<User> mp = userMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<User>()
                        .and(w -> w.like(User::getNickName, q).or().like(User::getUsername, q))
                        .orderByDesc(User::getId));
        PageSliceVO<UserBriefVO> vo = new PageSliceVO<>();
        vo.setPage(mp.getCurrent());
        vo.setSize(mp.getSize());
        vo.setTotal(mp.getTotal());
        vo.setHasMore(mp.getCurrent() * mp.getSize() < mp.getTotal());
        vo.setRecords(mp.getRecords().stream().map(this::toBrief).collect(Collectors.toList()));
        return vo;
    }

    private UserBriefVO toBrief(User u) {
        UserBriefVO vo = new UserBriefVO();
        vo.setId(u.getId());
        vo.setUsername(u.getUsername());
        vo.setNickName(u.getNickName());
        vo.setAvatar(u.getAvatar());
        return vo;
    }
}
