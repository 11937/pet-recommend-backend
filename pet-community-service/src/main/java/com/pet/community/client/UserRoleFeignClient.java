package com.pet.community.client;

import com.pet.common.entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * 查询用户角色；url 为空时走注册中心，非空时便于本地联调直连 user-service。
 */
@FeignClient(
        name = "pet-user-service",
        contextId = "communityUserRole",
        url = "${pet.user-service.url:}"
)
public interface UserRoleFeignClient {

    @GetMapping("/user/internal/{id}/role")
    Result<Map<String, Object>> getRole(@PathVariable("id") Long id);
}
