package com.pet.recommend.controller;


import com.pet.recommend.service.PetRecommendService;
import com.pet.common.dto.Pet;
import com.pet.common.dto.PetRecommendDTO;
import com.pet.common.entity.Result;

import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;


@RestController
@RequestMapping("/breed")
@CrossOrigin(origins = "*") // 跨域配置
public class PetRecommendController {

    @Resource
    private PetRecommendService petRecommendService;

    // 猫咪推荐接口（POST请求，接收筛选条件）
    @PostMapping("/recommend")
    public Result<List<Pet>> recommendCats(@RequestBody(required = false) PetRecommendDTO dto) {
        try {
            // 防御性处理：DTO为空时新建空对象，避免空指针
            if (dto == null) {
                dto = new PetRecommendDTO();
            }
            // 调用Service层推荐逻辑
            List<Pet> recommendList = petRecommendService.recommendPets(dto);

            // 无数据时返回友好提示（code仍为200，只是数据为空）
            if (recommendList.isEmpty()) {
                return new Result<>(200, "暂无符合条件的宠物推荐~", recommendList);
            }
            // 有数据时返回成功结果
            return new Result<>(200, "成功", recommendList);

        } catch (Exception e) {
            // 核心：打印完整异常栈（控制台能看到哪一行报错）
            e.printStackTrace();
            // 返回异常详情（方便定位问题）
            return new Result<>(500, "失败：" + e.getMessage(), null);
        }
    }
}
