package com.pet.community.client;

import com.pet.common.dto.NotificationPushDTO;
import com.pet.common.entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 用户服务内部接口：推送通知、拉取关注 id（关注流）。
 * Feign 出站由 FeignInternalSecretConfig 自动带 X-Internal-Secret。
 */
@FeignClient(
        name = "pet-user-service",
        contextId = "userSocialInternal",
        url = "${pet.user-service.url:}"
)
public interface UserSocialInternalFeignClient {

    @PostMapping("/user/internal/notifications")
    Result<Void> pushNotification(@RequestBody NotificationPushDTO dto);

    @GetMapping("/user/internal/social/following-ids")
    Result<List<Long>> listFollowingIds(@RequestParam("userId") Long userId);
}
