package com.pet.user.controller.elepet;

import com.pet.common.entity.Result;
import com.pet.common.util.JwtUtil;
import com.pet.user.dto.elepet.HealthRecordCreateDTO;
import com.pet.user.entity.elepet.HealthRecord;
import com.pet.user.entity.elepet.PetProfile;
import com.pet.user.service.elepet.HealthRecordService;
import com.pet.user.service.elepet.PetProfileService;
import com.pet.user.vo.elepet.HealthRecordVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/elePet/health")
public class HealthRecordController {

    @Resource
    private HealthRecordService healthRecordService;

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

    @PostMapping("/record")
    public Result<HealthRecordVO> addRecord(@RequestBody HealthRecordCreateDTO dto) {
        Long userId = getCurrentUserId();
        HealthRecordVO vo = healthRecordService.addRecord(userId, dto);
        return new Result<>(200, "记录成功", vo);
    }

    @GetMapping("/pet/{petId}")
    public Result<List<HealthRecordVO>> listRecords(
            @PathVariable Long petId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        Long userId = getCurrentUserId();
        // 可先校验宠物归属（省略，由 service 内部处理）
        List<HealthRecordVO> list = healthRecordService.listByPetId(petId, startDate, endDate);
        return new Result<>(200, "查询成功", list);
    }

    @Resource
    private PetProfileService petProfileService;
    @GetMapping("/pet/{petId}/trend")
    public Result<Map<String, Object>> getHealthTrend(
            @PathVariable Long petId,
            @RequestParam Integer recordType,
            @RequestParam String period) {
        Long userId = getCurrentUserId();

        // 1. 校验宠物归属
        PetProfile pet = petProfileService.getById(petId);
        if (pet == null || !pet.getUserId().equals(userId)) {
            return new Result<>(404, "宠物不存在或无权访问", null);
        }

        // 2. 根据 period 计算日期范围
        LocalDate endDate = LocalDate.now();
        LocalDate startDate;
        if ("week".equals(period)) {
            startDate = endDate.minusDays(6);
        } else if ("month".equals(period)) {
            startDate = endDate.minusDays(29);
        } else {
            return new Result<>(400, "period 参数应为 week 或 month", null);
        }

        // 3. 查询健康记录
        List<HealthRecord> records = healthRecordService.listByPetIdAndTypeAndDateRange(
                petId, recordType, startDate, endDate);

        // 4. 组装图表数据
        Map<String, Object> result = new HashMap<>();
        result.put("dates", records.stream().map(r -> r.getRecordDate().toString()).collect(Collectors.toList()));
        result.put("values", records.stream().map(HealthRecord::getValue).collect(Collectors.toList()));

        return new Result<>(200, "查询成功", result);
    }
}
