package com.pet.community.exception;

/**
 * 需要管理员（用户服务 role=ADMIN）时抛出，由全局异常处理映射为 HTTP 403。
 */
public class AdminAuthorizationException extends RuntimeException {

    public AdminAuthorizationException() {
        super("需要管理员权限");
    }
}
