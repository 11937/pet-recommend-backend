package com.pet.user.service.elepet;

import com.pet.user.dto.elepet.DecorationBuyDTO;
import com.pet.user.dto.elepet.DecorationBuyDTO;

import java.util.Map;
import java.util.Set;

public interface DecorationService {
    // 获取用户点数和已拥有的装饰品
    Map<String, Set<String>> getUserDecorations(Long userId);

    // 获取用户当前点数
    int getUserPoints(Long userId);

    // 购买装饰品
    void buyDecoration(Long userId, DecorationBuyDTO dto);
}
