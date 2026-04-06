package com.pet.user.controller;

import com.pet.common.dto.NotificationPushDTO;
import com.pet.common.entity.Result;
import com.pet.user.config.PetInternalApiProperties;
import com.pet.user.entity.User;
import com.pet.user.mapper.UserMapper;
import com.pet.user.vo.UserBriefVO;
import com.pet.user.service.UserFollowService;
import com.pet.user.service.UserNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 供其他服务（如社区）查询用户角色；勿对 C 端暴露或务必配合网关拦截 + 密钥。
 */
@RestController
@RequestMapping("/user/internal")
@RequiredArgsConstructor
public class UserInternalController {

    private final UserMapper userMapper;
    private final PetInternalApiProperties internalApiProperties;
    private final UserFollowService userFollowService;
    private final UserNotificationService userNotificationService;

    @GetMapping("/{id}/role")
    public Result<Map<String, String>> role(
            @PathVariable("id") Long id,
            @RequestHeader(value = "X-Internal-Secret", required = false) String secret) {
        if (!internalOk(secret)) {
            return new Result<>(403, "内部调用密钥无效", null);
        }
        User user = userMapper.selectById(id);
        if (user == null) {
            return new Result<>(404, "用户不存在", null);
        }
        String role = StringUtils.hasText(user.getRole()) ? user.getRole() : "USER";
        Map<String, String> body = new HashMap<>(2);
        body.put("role", role);
        return new Result<>("ok", body);
    }

    /** 供社区拉取「我关注的人」id 列表（关注流）。 */
    @GetMapping("/social/following-ids")
    public Result<List<Long>> followingIds(
            @RequestParam("userId") Long userId,
            @RequestHeader(value = "X-Internal-Secret", required = false) String secret) {
        if (!internalOk(secret)) {
            return new Result<>(403, "内部调用密钥无效", null);
        }
        if (userId == null) {
            return new Result<>(400, "userId 必填", null);
        }
        return new Result<>("ok", userFollowService.listFollowingIds(userId, 500));
    }

    /** 供社区 Feed 等批量拉取作者昵称、头像；最多 200 个 id。 */
    @PostMapping("/users/brief")
    public Result<List<UserBriefVO>> usersBrief(
            @RequestBody List<Long> ids,
            @RequestHeader(value = "X-Internal-Secret", required = false) String secret) {
        if (!internalOk(secret)) {
            return new Result<>(403, "内部调用密钥无效", null);
        }
        if (ids == null || ids.isEmpty()) {
            return new Result<>("ok", new ArrayList<>());
        }
        List<Long> uniq = ids.stream().filter(Objects::nonNull).distinct().limit(200).collect(Collectors.toList());
        if (uniq.isEmpty()) {
            return new Result<>("ok", new ArrayList<>());
        }
        List<User> users = userMapper.selectBatchIds(uniq);
        List<UserBriefVO> out = new ArrayList<>(users.size());
        for (User u : users) {
            UserBriefVO vo = new UserBriefVO();
            vo.setId(u.getId());
            vo.setUsername(u.getUsername());
            vo.setNickName(u.getNickName());
            vo.setAvatar(u.getAvatar());
            out.add(vo);
        }
        return new Result<>("ok", out);
    }

    /** 供社区等服务写入通知（评论、点赞等）。 */
    @PostMapping("/notifications")
    public Result<Void> pushNotification(
            @RequestBody NotificationPushDTO dto,
            @RequestHeader(value = "X-Internal-Secret", required = false) String secret) {
        if (!internalOk(secret)) {
            return new Result<>(403, "内部调用密钥无效", null);
        }
        userNotificationService.push(dto);
        return new Result<>("ok", null);
    }

    private boolean internalOk(String secret) {
        if (!StringUtils.hasText(internalApiProperties.getApiSecret())) {
            return true;
        }
        return internalApiProperties.getApiSecret().equals(secret);
    }
}
