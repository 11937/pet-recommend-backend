package com.cat_card.config;

import com.cat_card.util.SwaggerProperties;
import com.github.xiaoymin.swaggerbootstrapui.annotations.EnableSwaggerBootstrapUI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
@EnableSwaggerBootstrapUI
public class SwaggerConfig {

    @Autowired
    private SwaggerProperties properties;

    @Bean
    public Docket createRestApi() {

        // 设置请求头
//        List<Parameter> parameters = new ArrayList<>();
//        parameters.add(new ParameterBuilder()
//                .name("Authorization") // 字段名
//                .description("Authorization") // 描述
//                .modelRef(new ModelRef("string")) // 数据类型
//                .parameterType("header") // 参数类型
//                .defaultValue("") // 默认值：可自己设置
//                .hidden(true) // 是否隐藏
//                .required(false) // 是否必须
//                .build());

        return new Docket(DocumentationType.SWAGGER_2)
                //用于生成API信息
                .apiInfo(apiInfo())
                //select()函数返回一个ApiSelectorBuilder实例,用来控制接口被swagger做成文档
                .select()
                //用于指定扫描哪个包下的接口
                //.apis(RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class))
                .apis(RequestHandlerSelectors.basePackage("com.cat_card"))
                //选择所有的API,如果你想只为部分API生成文档，可以配置这里
                .paths(PathSelectors.any())
                .build();
        // 添加请求头参数
//                .globalOperationParameters(parameters);
    }

    /*
     *用于定义API主界面的信息，比如可以声明所有的API的总标题、描述、版本
     */
    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                //用来自定义API的标题
                .title(properties.getTitle())
                //用来描述整体的API
                .description(properties.getDescription())
                //可以用来定义版本
                .version(properties.getVersion())
                //用于定义服务的域名
                .termsOfServiceUrl(properties.getTermsOfServiceUrl())
                .license(properties.getLicense())
                .licenseUrl(properties.getLicenseUrl())
                //创建人信息
                .contact(new Contact(
                        properties.getContactName(),
                        String.format(properties.getContactUrl()),
                        properties.getContactEmail()
                ))
                .build();
    }
}
