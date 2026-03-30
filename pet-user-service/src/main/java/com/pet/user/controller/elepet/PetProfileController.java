package com.pet.user.controller.elepet;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pet.common.entity.Result;
import com.pet.common.util.JwtUtil;
import com.pet.user.dto.elepet.PetProfileCreateDTO;
import com.pet.user.entity.elepet.PetModel;
import com.pet.user.entity.elepet.PetProfile;
import com.pet.user.mapper.elepet.PetModelMapper;
import com.pet.user.service.elepet.DecorationService;
import com.pet.user.service.elepet.PetProfileService;
import com.pet.user.vo.elepet.PetProfileVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/elePet")
public class PetProfileController {

    @Resource
    private DecorationService decorationService;

    @Resource
    private PetProfileService petProfileService;

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

    //用户创建宠物档案
    @PostMapping("/create")
    public Result<PetProfileVO> createPet(@RequestBody PetProfileCreateDTO dto) {
        Long userId = getCurrentUserId();
        PetProfile pet = petProfileService.createPet(userId, dto);
        return new Result<>(200, "创建成功", PetProfileVO.from(pet));
    }

    //查询用户拥有的宠物档案
    @GetMapping("/list")
    public Result<List<PetProfileVO>> listPets() {
        Long userId = getCurrentUserId();
        List<PetProfile> pets = petProfileService.lambdaQuery()
                .eq(PetProfile::getUserId, userId)
                .list();
        List<PetProfileVO> voList = pets.stream()
                .map(PetProfileVO::from)
                .collect(Collectors.toList());
        return new Result<>(200, "查询成功", voList);
    }

    //查询用户拥有的某个宠物详细档案
    @GetMapping("/{petId}")
    public Result<PetProfileVO> getPet(@PathVariable Long petId) {
        Long userId = getCurrentUserId();
        PetProfile pet = petProfileService.getById(petId);
        if (pet == null || !pet.getUserId().equals(userId)) {
            return new Result<>(404, "宠物不存在或无权访问", null);
        }
        return new Result<>(200, "查询成功", PetProfileVO.from(pet));
    }


    //给用户选择的宠物档案更换装扮
    @PutMapping("/{petId}/decoration")
    public Result<Void> updateDecoration(@PathVariable Long petId, @RequestBody Map<String, String> decoration) {
        Long userId = getCurrentUserId();

        // 1. 检查宠物是否存在且属于当前用户
        PetProfile pet = petProfileService.getById(petId);
        if (pet == null || !pet.getUserId().equals(userId)) {
            return new Result<>(404, "宠物不存在或无权操作", null);
        }

        // 2. 可选：校验用户是否拥有这些装饰品
        // 获取用户已拥有的装饰品
        Map<String, Set<String>> owned = decorationService.getUserDecorations(userId);

        // 检查 bodyColor
        String bodyColor = decoration.get("bodyColor");
        if (bodyColor != null && !owned.getOrDefault("bodyColor", Collections.emptySet()).contains(bodyColor)) {
            return new Result<>(400, "未拥有该颜色", null);
        }
        // 检查 pattern
        String pattern = decoration.get("pattern");
        if (pattern != null && !owned.getOrDefault("pattern", Collections.emptySet()).contains(pattern)) {
            return new Result<>(400, "未拥有该花纹", null);
        }
        // 检查 accessory
        String accessory = decoration.get("accessory");
        if (accessory != null && !owned.getOrDefault("accessory", Collections.emptySet()).contains(accessory)) {
            return new Result<>(400, "未拥有该饰品", null);
        }
        // 检查 background
        String background = decoration.get("background");
        if (background != null && !owned.getOrDefault("background", Collections.emptySet()).contains(background)) {
            return new Result<>(400, "未拥有该背景", null);
        }

        // 3. 保存装扮到数据库
        String decorationJson = JSON.toJSONString(decoration);
        pet.setDecoration(decorationJson);
        petProfileService.updateById(pet);

        return new Result<>(200, "保存成功", null);
    }

    // 组装宠物模型返回给前端，包括基本模型和装扮
    @Resource
    private PetModelMapper petModelMapper;
    @GetMapping("/{petId}/render")
    public Result<Map<String, Object>> getPetRenderData(@PathVariable Long petId) {
        Long userId = getCurrentUserId();

        // 1. 获取宠物档案
        PetProfile pet = petProfileService.getById(petId);
        if (pet == null || !pet.getUserId().equals(userId)) {
            return new Result<>(404, "宠物不存在或无权访问", null);
        }

        // 2. 根据品类获取模型
        PetModel model = petModelMapper.selectOne(
                new LambdaQueryWrapper<PetModel>().eq(PetModel::getCategory, pet.getCategory())
        );
        if (model == null) {
            return new Result<>(404, "未找到该品类对应的宠物模型", null);
        }

        // 3. 解析当前装扮（如果没有装扮，用模型的默认值）
        Map<String, String> decoration = new HashMap<>();
        if (pet.getDecoration() != null && !pet.getDecoration().isEmpty()) {
            decoration = JSON.parseObject(pet.getDecoration(), new TypeReference<Map<String, String>>() {});
        } else {
            // 默认使用模型的默认颜色和花纹
            decoration.put("bodyColor", model.getDefaultColor());
            decoration.put("pattern", model.getDefaultPattern());
        }

        // 4. 获取用户点数和已拥有的装饰品
        int points = decorationService.getUserPoints(userId);
        Map<String, Set<String>> owned = decorationService.getUserDecorations(userId);

        // 5. 组装返回数据
        Map<String, Object> result = new HashMap<>();
        result.put("model", model);           // 模型数据（包含 svgPath 等）
        result.put("decoration", decoration); // 当前装扮
        result.put("points", points);         // 用户当前点数
        result.put("owned", owned);           // 用户已拥有的装饰品
        result.put("petName", pet.getName()); // 宠物昵称

        return new Result<>(200, "查询成功", result);
    }
}