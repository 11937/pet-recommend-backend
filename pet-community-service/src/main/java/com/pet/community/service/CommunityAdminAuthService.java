package com.pet.community.service;

import com.pet.common.entity.Result;
import com.pet.community.client.UserRoleFeignClient;
import com.pet.community.exception.AdminAuthorizationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 管理员校验：用户服务 role 必须为 ADMIN（与 JWT subject 用户 id 对应）。
 */
@Service
@RequiredArgsConstructor
public class CommunityAdminAuthService {

    private static final String ADMIN_ROLE = "ADMIN";

    private final UserRoleFeignClient userRoleFeignClient;

    public void requireAdmin(long userId) {
        if (!isAdmin(userId)) {
            throw new AdminAuthorizationException();
        }
    }

    public boolean isAdmin(long userId) {
        Result<Map<String, Object>> res = userRoleFeignClient.getRole(userId);
        if (res == null || res.getCode() != 200 || res.getData() == null) {
            return false;
        }
        Object role = res.getData().get("role");
        return ADMIN_ROLE.equals(String.valueOf(role));
    }
}
