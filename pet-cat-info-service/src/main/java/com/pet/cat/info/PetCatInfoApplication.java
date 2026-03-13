package com.pet.cat.info;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = "com.pet")
@EnableDiscoveryClient // 开启服务注册发现
public class PetCatInfoApplication {
    public static void main(String[] args) {
        SpringApplication.run(PetCatInfoApplication.class, args);
    }

}
