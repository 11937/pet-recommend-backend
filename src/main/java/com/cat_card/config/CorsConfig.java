package com.cat_card.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 跨域配置类
 */
@Configuration
public class CorsConfig {

    /**
     * 配置跨域过滤器
     */
    @Bean
    public CorsFilter corsFilter() {
        // 1. 创建跨域配置对象
        CorsConfiguration config = new CorsConfiguration();
        // 允许的源（* 表示允许所有前端域名，生产环境建议指定具体域名，如 http://localhost:8080）
        config.addAllowedOriginPattern("*");
        // 允许携带Cookie（前后端分离场景常用）
        config.setAllowCredentials(true);
        // 允许的请求方法（GET、POST、PUT、DELETE等）
        config.addAllowedMethod("*");
        // 允许的请求头（* 表示所有）
        config.addAllowedHeader("*");
        // 暴露的响应头（前端可以获取的自定义头）
        config.addExposedHeader("Authorization");

        // 2. 配置跨域规则的匹配路径（/** 表示所有接口）
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        // 3. 返回跨域过滤器
        return new CorsFilter(source);
    }
}
