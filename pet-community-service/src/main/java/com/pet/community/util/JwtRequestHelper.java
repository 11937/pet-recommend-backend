package com.pet.community.util;

import com.pet.common.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/** 从 Authorization: Bearer 解析当前用户；与 user 服务约定一致。 */
@Component
@RequiredArgsConstructor
public class JwtRequestHelper {

    private final JwtUtil jwtUtil;

    /** 未登录或 token 无效时返回 null。 */
    public Long currentUserIdOrNull() {
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
        if (!jwtUtil.validateToken(token)) {
            return null;
        }
        return jwtUtil.getUserIdFromToken(token);
    }

    /** 必须登录；否则抛 IllegalStateException(UNAUTHORIZED)，由全局异常处理转 401。 */
    public Long requireUserId() {
        Long uid = currentUserIdOrNull();
        if (uid == null) {
            throw new IllegalStateException("UNAUTHORIZED");
        }
        return uid;
    }
}
