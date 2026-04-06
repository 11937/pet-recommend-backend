package com.pet.community.web;

import com.pet.common.entity.Result;
import com.pet.community.service.CommunityAdminAuthService;
import com.pet.community.service.CommunityHotSnapshotService;
import com.pet.community.service.CommunityPostService;
import com.pet.community.util.JwtRequestHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.ZoneId;

/**
 * 管理端：需登录且用户服务 role=ADMIN；下架会写审计表。
 */
@RestController
@RequestMapping("/community/admin")
@RequiredArgsConstructor
public class CommunityAdminController {

    private static final ZoneId SHANGHAI = ZoneId.of("Asia/Shanghai");

    private final CommunityPostService postService;
    private final CommunityAdminAuthService adminAuthService;
    private final JwtRequestHelper jwtRequestHelper;
    private final CommunityHotSnapshotService hotSnapshotService;

    @PostMapping("/posts/{id}/offline")
    public Result<Void> adminOffline(
            @PathVariable("id") long id,
            @RequestParam(value = "reason", required = false) String reason) {
        long uid = jwtRequestHelper.requireUserId();
        adminAuthService.requireAdmin(uid);
        postService.adminOfflineByAdmin(uid, id, reason);
        return new Result<>(200, "管理员下架成功");
    }

    /**
     * 手动重建某日互动榜与顶部 2 卡位（默认同上海「今天」）；定时任务未跑或补数据时用。
     */
    @PostMapping("/hot/rebuild")
    public Result<Void> rebuildHot(
            @RequestParam(value = "date", required = false) String date) {
        long uid = jwtRequestHelper.requireUserId();
        adminAuthService.requireAdmin(uid);
        LocalDate d = date == null || date.isEmpty()
                ? LocalDate.now(SHANGHAI)
                : LocalDate.parse(date);
        hotSnapshotService.buildSnapshotForShanghaiDate(d);
        return new Result<>(200, "日榜已重建");
    }
}
