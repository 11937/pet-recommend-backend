package com.pet.user.controller;

import com.pet.common.entity.Result;
import com.pet.common.vo.PageSliceVO;
import com.pet.user.dto.DmSendDTO;
import com.pet.user.service.UserDmService;
import com.pet.user.util.JwtBearerHelper;
import com.pet.user.vo.DmMessageVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/user/dm")
@RequiredArgsConstructor
public class UserDmController {

    private final UserDmService userDmService;
    private final JwtBearerHelper jwtBearerHelper;

    @PostMapping("/send")
    public Result<DmMessageVO> send(@Valid @RequestBody DmSendDTO dto) {
        Long uid = jwtBearerHelper.getUserIdOrNull();
        if (uid == null) {
            return new Result<>(401, "未登录", null);
        }
        try {
            return new Result<>("ok", userDmService.send(uid, dto));
        } catch (IllegalArgumentException e) {
            return new Result<>(400, e.getMessage(), null);
        }
    }

    @GetMapping("/conversation/{otherUserId}")
    public Result<PageSliceVO<DmMessageVO>> conversation(
            @PathVariable long otherUserId,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size) {
        Long uid = jwtBearerHelper.getUserIdOrNull();
        if (uid == null) {
            return new Result<>(401, "未登录", null);
        }
        userDmService.markConversationRead(uid, otherUserId);
        return new Result<>("ok", userDmService.pageConversation(uid, otherUserId, page, size));
    }

    @GetMapping("/unread-count")
    public Result<Long> unreadCount() {
        Long uid = jwtBearerHelper.getUserIdOrNull();
        if (uid == null) {
            return new Result<>(401, "未登录", null);
        }
        return new Result<>("ok", userDmService.unreadCount(uid));
    }
}
