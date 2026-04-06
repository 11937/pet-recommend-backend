package com.pet.user.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 微服务间内部调用：非空时要求请求头 X-Internal-Secret 与配置一致。
 */
@Data
@Component
@ConfigurationProperties(prefix = "pet.internal")
public class PetInternalApiProperties {

    private String apiSecret = "";
}
