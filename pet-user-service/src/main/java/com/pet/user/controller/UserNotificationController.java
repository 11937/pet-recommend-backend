package com.pet.user.controller;

import com.pet.common.entity.Result;
import com.pet.common.vo.PageSliceVO;
import com.pet.user.service.UserNotificationService;
import com.pet.user.util.JwtBearerHelper;
import com.pet.user.vo.UserNotificationVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user/notifications")
@RequiredArgsConstructor
public class UserNotificationController {

    private final UserNotificationService userNotificationService;
    private final JwtBearerHelper jwtBearerHelper;

    @GetMapping
    public Result<PageSliceVO<UserNotificationVO>> page(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size) {
        Long uid = jwtBearerHelper.getUserIdOrNull();
        if (uid == null) {
            return new Result<>(401, "未登录", null);
        }
        return new Result<>("ok", userNotificationService.page(uid, page, size));
    }

    @GetMapping("/unread-count")
    public Result<Long> unreadCount() {
        Long uid = jwtBearerHelper.getUserIdOrNull();
        if (uid == null) {
            return new Result<>(401, "未登录", null);
        }
        return new Result<>("ok", userNotificationService.unreadCount(uid));
    }

    @PostMapping("/{id}/read")
    public Result<Void> markRead(@PathVariable long id) {
        Long uid = jwtBearerHelper.getUserIdOrNull();
        if (uid == null) {
            return new Result<>(401, "未登录", null);
        }
        userNotificationService.markRead(uid, id);
        return new Result<>("ok", null);
    }

    @PostMapping("/read-all")
    public Result<Void> markAllRead() {
        Long uid = jwtBearerHelper.getUserIdOrNull();
        if (uid == null) {
            return new Result<>(401, "未登录", null);
        }
        userNotificationService.markAllRead(uid);
        return new Result<>("ok", null);
    }
}
