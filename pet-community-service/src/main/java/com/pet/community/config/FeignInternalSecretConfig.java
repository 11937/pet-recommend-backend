package com.pet.community.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/** Feign 出站请求自动带上 X-Internal-Secret，与用户服务内部接口校验一致。 */
@Configuration
public class FeignInternalSecretConfig {

    @Bean
    public RequestInterceptor internalSecretInterceptor(PetInternalApiProperties props) {
        return template -> {
            if (StringUtils.hasText(props.getApiSecret())) {
                template.header("X-Internal-Secret", props.getApiSecret());
            }
        };
    }
}
