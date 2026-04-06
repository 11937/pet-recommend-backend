package com.pet.community.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** 调用用户服务内部接口时携带 X-Internal-Secret；与用户服务 pet.internal.api-secret 一致。 */
@Data
@Component
@ConfigurationProperties(prefix = "pet.internal")
public class PetInternalApiProperties {

    private String apiSecret = "";
}
