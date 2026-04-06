package com.pet.community.web;

import com.pet.common.entity.Result;
import com.pet.community.dto.PostAuditRequestDTO;
import com.pet.community.service.CommunityAdminAuthService;
import com.pet.community.service.CommunityPostService;
import com.pet.community.util.JwtRequestHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * 审核：通过/拒绝（改 audit_status），需管理员 JWT。
 */
@RestController
@RequestMapping("/community/audit")
@RequiredArgsConstructor
public class CommunityAuditController {

    private final CommunityPostService postService;
    private final CommunityAdminAuthService adminAuthService;
    private final JwtRequestHelper jwtRequestHelper;

    @PostMapping("/posts/{id}/review")
    public Result<Void> review(
            @PathVariable("id") long id,
            @Valid @RequestBody PostAuditRequestDTO body) {
        long uid = jwtRequestHelper.requireUserId();
        adminAuthService.requireAdmin(uid);
        String d = body.getDecision() == null ? "" : body.getDecision().trim();
        boolean approve;
        if ("APPROVE".equalsIgnoreCase(d)) {
            approve = true;
        } else if ("REJECT".equalsIgnoreCase(d)) {
            approve = false;
        } else {
            return new Result<>(400, "decision 仅支持 APPROVE 或 REJECT", null);
        }
        postService.adminSetAudit(uid, id, approve, body.getRemark());
        return new Result<>(200, approve ? "已通过" : "已拒绝");
    }
}
