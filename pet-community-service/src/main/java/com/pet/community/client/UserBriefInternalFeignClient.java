package com.pet.community.client;

import com.pet.common.entity.Result;
import com.pet.community.client.dto.UserBriefJson;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * 批量拉取用户昵称、头像（内部接口，带 X-Internal-Secret）。
 */
@FeignClient(
        name = "pet-user-service",
        contextId = "userBriefInternal",
        url = "${pet.user-service.url:}"
)
public interface UserBriefInternalFeignClient {

    @PostMapping("/user/internal/users/brief")
    Result<List<UserBriefJson>> usersBrief(@RequestBody List<Long> userIds);
}
