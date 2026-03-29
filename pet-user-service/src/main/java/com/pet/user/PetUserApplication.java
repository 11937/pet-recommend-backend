package com.pet.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;


@SpringBootApplication(scanBasePackages = {"com.pet.common","com.pet.user"})
@EnableDiscoveryClient // 开启服务注册发现
public class PetUserApplication {
    public static void main(String[] args) {

        SpringApplication.run(PetUserApplication.class, args);
    }

}

