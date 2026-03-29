package com.pet.info;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = {"com.pet.info", "com.pet.common"})
@EnableDiscoveryClient // 开启服务注册发现
public class PetInfoApplication {
    public static void main(String[] args) {

        SpringApplication.run(PetInfoApplication.class, args);
    }

}
