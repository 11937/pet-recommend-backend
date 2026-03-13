package com.pet.cat.info.controller;




import com.pet.cat.info.service.CatSearchService;
import com.pet.common.dto.Cat;
import com.pet.common.entity.Result;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.List;

@CrossOrigin
@RestController  //声明控制器 响应请求
@RequestMapping("/breed")  //请求映射父路径
public class CatController {

    //注入service接口
    @Resource
    private CatSearchService catService;

    @PostMapping ("/list")
    public Result<List<Cat>> findCatList(@RequestBody Cat cat) {

        try {
            return new Result<List<Cat>>(200, "成功", catService.findCatList(cat));
        } catch (Exception e) {
            e.printStackTrace();
            return new Result<List<Cat>>(500, "失败");

        }
    }

}
