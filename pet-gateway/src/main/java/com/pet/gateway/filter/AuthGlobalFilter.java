package com.pet.gateway.filter;


import com.pet.gateway.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.PathContainer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;


@Slf4j
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;

    // 构造注入
    public AuthGlobalFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    // 2. 白名单放行
    private static final PathPatternParser parser = new PathPatternParser();
    private static final List<PathPattern> WHITE_LIST = Arrays.asList(
            parser.parse("/api/user/**"),
            parser.parse("/api/breed/**"),
            parser.parse("/api/recommend/**")
    );   // 支持通配符

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 1. 获取请求路径（网关写法）
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();
        log.info("请求路径：{}", path);

        // ====================== 白名单放行 ======================
        for (PathPattern pattern : WHITE_LIST) {
            if (pattern.matches(PathContainer.parsePath(path))) {
                log.info("✅ 白名单放行：{}", path);
                return chain.filter(exchange);
            }
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