package com.pet.cat.info.controller;

import com.pet.cat.info.service.AllCatInfoService;
import com.pet.common.dto.Cat;
import com.pet.common.entity.Result;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.List;

@CrossOrigin
@RestController  //声明控制器 响应请求
@RequestMapping("/breed")  //请求映射父路径
public class AllCatController {

    @Resource
    private AllCatInfoService allCatList;

    @GetMapping("/allCats")
    public Result<List<Cat>> findAllCatList(){
        try {
            return new Result<List<Cat>>(200, "成功", allCatList.findAllCatList());
        } catch (Exception e) {
            e.printStackTrace();
            return new Result<List<Cat>>(500, "失败");
        }
    }

}
