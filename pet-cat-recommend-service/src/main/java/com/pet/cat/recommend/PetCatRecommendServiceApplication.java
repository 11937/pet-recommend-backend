package com.pet.cat.recommend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
@EnableFeignClients(basePackages = "com.pet.cat.recommend.feign")
public class PetCatRecommendServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(PetCatRecommendServiceApplication.class, args);
    }

}
