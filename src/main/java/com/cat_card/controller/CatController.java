package com.cat_card.controller;

import com.cat_card.pojo.Cat;
import com.cat_card.service.CatService;
import com.cat_card.util.ResultUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@Api(tags = "猫咪推荐")
@RestController  //声明控制器 响应请求
@RequestMapping("/breed")  //请求映射父路径
public class CatController {

    //注入service接口
    @Autowired
    private CatService catService;

    @ApiOperation(value = "查询猫咪",httpMethod = "POST",notes = "查询猫咪信息")
    @PostMapping ("/list")
    public ResultUtil<List<Cat>> findCatList(@RequestBody Cat cat) {

        try {
            return new ResultUtil<List<Cat>>(200, "成功", catService.findCatList(cat));
        } catch (Exception e) {
            e.printStackTrace();
            return new ResultUtil<List<Cat>>(500, "失败");

        }
    }

}
