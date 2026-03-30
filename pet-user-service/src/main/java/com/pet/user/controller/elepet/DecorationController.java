package com.pet.user.controller.elepet;

import com.pet.common.entity.Result;
import com.pet.common.util.JwtUtil;
import com.pet.user.dto.elepet.DecorationBuyDTO;
import com.pet.user.entity.elepet.DecorationItem;
import com.pet.user.mapper.elepet.DecorationItemMapper;
import com.pet.user.service.elepet.DecorationService;
import com.pet.user.vo.elepet.DecorationItemVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/elePet/decoration")
public class DecorationController {

    @Resource
    private DecorationService decorationService;

    @Resource
    private JwtUtil jwtUtil;

    private Long getCurrentUserId() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) {
                return null;
            }

            HttpServletRequest request = attrs.getRequest();
            // 从标准头获取
            String auth = request.getHeader("Authorization");

            if (auth == null || !auth.startsWith("Bearer ")) {
                return null;
            }

            // 去掉 "Bearer " 前缀
            String token = auth.substring(7);
            return jwtUtil.getUserIdFromToken(token);

        } catch (Exception e) {
            log.error("获取用户ID失败", e);
            return null;
        }
    }

    // 获取用户点数和已拥有的装饰品
    @GetMapping("/info")
    public Result<Map<String, Object>> getUserDecorationInfo() {
        Long userId = getCurrentUserId();
        int points = decorationService.getUserPoints(userId);
        Map<String, Set<String>> owned = decorationService.getUserDecorations(userId);

        Map<String, Object> result = new HashMap<>();
        result.put("points", points);
        result.put("owned", owned);
        return new Result<>(200, "查询成功", result);
    }

    // 购买装饰品
    @PostMapping("/buy")
    public Result<Void> buyDecoration(@RequestBody DecorationBuyDTO dto) {
        Long userId = getCurrentUserId();
        decorationService.buyDecoration(userId, dto);
        return new Result<>(200, "购买成功", null);
    }

    @Resource
    private DecorationItemMapper decorationItemMapper;
    @GetMapping("/items")
    public Result<List<DecorationItemVO>> getDecorationItems() {
        List<DecorationItem> items = decorationItemMapper.selectList(null);
        List<DecorationItemVO> voList = items.stream()
                .map(item -> {
                    DecorationItemVO vo = new DecorationItemVO();
                    vo.setType(item.getType());
                    vo.setItemId(item.getItemId());
                    vo.setName(item.getName());
                    vo.setCostPoints(item.getCostPoints());
                    vo.setImageUrl(item.getImageUrl());
                    return vo;
                })
                .collect(Collectors.toList());
        return new Result<>(200, "查询成功", voList);
    }
}
