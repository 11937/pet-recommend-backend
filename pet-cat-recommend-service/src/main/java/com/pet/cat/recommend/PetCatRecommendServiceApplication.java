package com.pet.cat.recommend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
@EnableFeignClients(basePackages = "com.pet.cat.recommend.feign")
@ComponentScan(basePackages = "com.pet")
public class PetCatRecommendServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(PetCatRecommendServiceApplication.class, args);
    }

}
