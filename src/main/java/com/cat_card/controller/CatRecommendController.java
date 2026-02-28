package com.cat_card.controller;

import com.cat_card.pojo.Cat;
import com.cat_card.service.CatRecommendService;
import com.cat_card.util.ResultUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.cat_card.pojo.CatRecommendDTO;

import java.util.List;


@RestController
@RequestMapping("/breed")
@CrossOrigin(origins = "*") // 跨域配置
public class CatRecommendController {

    @Autowired
    private CatRecommendService catRecommendService;

    // 猫咪推荐接口（POST请求，接收筛选条件）
    @PostMapping("/recommend")
    public ResultUtil<List<Cat>> recommendCats(@RequestBody CatRecommendDTO dto) {
        try {
            List<Cat> recommendList = catRecommendService.recommendCats(dto);
            if (recommendList.isEmpty()) {
                return new ResultUtil<List<Cat>>("暂无符合条件的猫咪推荐~",recommendList);
            }
            // 调用方法，返回成功结果（带猫咪列表数据）
            return new  ResultUtil<List<Cat>>(200, "成功",recommendList);
        } catch (Exception e) {
            // 异常时返回失败提示
            return  new ResultUtil<List<Cat>>(500, "失败");
        }
    }
}

