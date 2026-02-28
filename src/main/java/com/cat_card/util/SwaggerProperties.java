package com.cat_card.util;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Data
@Component
public class SwaggerProperties {

    @Value("${swagger.profiles.title:默认标题}")
    private String title;
    @Value("${swagger.profiles.description:默认描述}")
    private String description;
    @Value("${swagger.profiles.version:1.0}")
    private String version;
    @Value("${swagger.profiles.termsOfServiceUrl:}")
    private String termsOfServiceUrl;
    @Value("${swagger.profiles.license:}")
    private String license;
    @Value("${swagger.profiles.licenseUrl:}")
    private String licenseUrl;
    @Value("${swagger.port:${server.port}}")
    private String port;
    @Value("${swagger.addr:localhost}")
    private String addr;
    @Value("${swagger.contact.name:}")
    private String contactName;
    @Value("${swagger.contact.email:}")
    private String contactEmail;
    @Value("${swagger.contact.url:}")
    private String contactUrl;

}