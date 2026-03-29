package com.pet.gateway.filter;


import com.pet.gateway.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


@Slf4j
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;

    // 构造注入
    public AuthGlobalFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 1. 获取请求路径（网关写法）
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();

        // 2. 白名单放行
        if (path.equals("/user/login") || path.equals("/user/register")) {
            return chain.filter(exchange);
        }

        // 3. 提取 token（网关写法）
        String bearer = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        String token = resolveToken(bearer);
        log.info("提取到的 token: {}", token);

        // 4. 校验 token（和你原来逻辑一样）
        if (token != null) {
            boolean valid = jwtUtil.validateToken(token);
            log.info("token 验证结果: {}", valid);
            if (valid) {
                Long userId = jwtUtil.getUserIdFromToken(token);
                log.info("token 有效，用户ID: {}", userId);

            } else {
                log.warn("token 无效或已过期");
            }
        } else {
            log.warn("未提取到 token");
        }

        // 5. 放行（网关写法）
        return chain.filter(exchange);
    }

    // 你的原逻辑，完全不变
    private String resolveToken(String bearer) {
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }

    @Override
    public int getOrder() {
        return -100;
    }
}