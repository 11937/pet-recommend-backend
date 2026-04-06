package com.pet.community;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 社区服务入口：帖子、Feed、点赞收藏等。
 * 扫描 com.pet.common 以复用 JwtUtil 等公共 Bean。
 */
@SpringBootApplication(scanBasePackages = {"com.pet.community", "com.pet.common"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.pet.community.client")
@EnableScheduling
@MapperScan("com.pet.community.mapper")
public class PetCommunityApplication {

    public static void main(String[] args) {
        SpringApplication.run(PetCommunityApplication.class, args);
    }
}
