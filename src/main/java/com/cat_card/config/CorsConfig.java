package com.cat_card.config;

import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
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

    // ===== 新增：HTTP 重定向到 HTTPS 的配置（复制这段即可） =====
    @Bean
    public ServletWebServerFactory servletContainer() {
        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory() {
            @Override
            protected void postProcessContext(Context context) {
                SecurityConstraint securityConstraint = new SecurityConstraint();
                securityConstraint.setUserConstraint("CONFIDENTIAL");
                SecurityCollection collection = new SecurityCollection();
                collection.addPattern("/*");
                securityConstraint.addCollection(collection);
                context.addConstraint(securityConstraint);
            }
        };
        tomcat.addAdditionalTomcatConnectors(redirectConnector());
        return tomcat;
    }

    private Connector redirectConnector() {
        Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
        connector.setScheme("http");
        connector.setPort(8080); // 你的后端原HTTP端口
        connector.setSecure(false);
        connector.setRedirectPort(443); // HTTPS默认端口
        return connector;
    }
}
