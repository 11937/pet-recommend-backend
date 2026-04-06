package com.pet.user.service;

import com.pet.common.vo.PageSliceVO;
import com.pet.user.vo.UserBriefVO;

public interface UserSearchService {

    PageSliceVO<UserBriefVO> searchUsers(String keyword, long page, long size);
}
