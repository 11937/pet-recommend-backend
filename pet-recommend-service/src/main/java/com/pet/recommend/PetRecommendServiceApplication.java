package com.pet.recommend;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@EnableFeignClients(basePackages = "com.pet.recommend.feign")
@SpringBootApplication(scanBasePackages = {"com.pet.recommend", "com.pet.common"},
        exclude = DataSourceAutoConfiguration.class
)
public class PetRecommendServiceApplication {
    public static void main(String[] args) {

        SpringApplication.run(PetRecommendServiceApplication.class, args);
    }
    @Bean
    public ApplicationRunner runner(ApplicationContext context) {
        return args -> {
            Map<String, Object> beans = context.getBeansWithAnnotation(RestController.class);
            System.out.println("Registered RestControllers: " + beans.keySet());
        };
    }
}
