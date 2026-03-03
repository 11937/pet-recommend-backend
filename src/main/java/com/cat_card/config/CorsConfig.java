package com.cat_card.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        // 1. 创建跨域配置对象
        CorsConfiguration config = new CorsConfiguration();

        // ========== 添加 Vercel/Netlify/本地 域名 ==========
        // 注意：2.7.6 用 addAllowedOriginPattern，不要用 addAllowedOrigin（已过时）
        //config.addAllowedOriginPattern("https://pet-recommend-frontend.vercel.app"); // Vercel
        //config.addAllowedOriginPattern("https://deluxe-crostata-8dafc2.netlify.app"); // Netlify
        config.addAllowedOriginPattern("http://localhost:1011/**"); // 本地前端
        config.addAllowedOriginPattern("http://10.80.214.86:1011"); // 本地IP前端
        config.addAllowedOriginPattern("https://11937.github.io");
        config.addAllowedOriginPattern("http://localhost:1011/pet-recommend-frontend");
        // ========== 必配项（跨域核心） ==========
        config.setAllowCredentials(true); // 允许携带Cookie（跨域必须开）
        config.addAllowedMethod("*");     // 允许所有请求方法（GET/POST/PUT等）
        config.addAllowedHeader("*");     // 允许所有请求头
        config.setMaxAge(3600L);          // 预检请求缓存1小时，减少重复OPTIONS请求

        // 2. 配置生效路径（所有接口都生效）
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config); // /** 表示所有接口

        // 3. 返回跨域过滤器
        return new CorsFilter(source);
    }
}