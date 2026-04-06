package com.pet.user.util;

import com.pet.common.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/** 从 Authorization Bearer 解析当前用户 ID。 */
@Slf4j
@Component
public class JwtBearerHelper {

    @Resource
    private JwtUtil jwtUtil;

    public Long getUserIdOrNull() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) {
                return null;
            }
            HttpServletRequest request = attrs.getRequest();
            String auth = request.getHeader("Authorization");
            if (auth == null || !auth.startsWith("Bearer ")) {
                return null;
            }
            String token = auth.substring(7);
            return jwtUtil.getUserIdFromToken(token);
        } catch (Exception e) {
            log.debug("解析用户失败: {}", e.getMessage());
            return null;
        }
    }

    public long requireUserId() {
        Long uid = getUserIdOrNull();
        if (uid == null) {
            throw new IllegalStateException("未登录");
        }
        return uid;
    }
}
