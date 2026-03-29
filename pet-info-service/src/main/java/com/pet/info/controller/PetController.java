package com.pet.info.controller;


import com.pet.common.dto.Pet;
import com.pet.common.entity.Result;
import com.pet.info.service.PetSearchService;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.List;

@CrossOrigin
@RestController  //声明控制器 响应请求
@RequestMapping("/breed")  //请求映射父路径
public class PetController {

    //注入service接口
    @Resource
    private PetSearchService petService;

    @PostMapping ("/list")
    public Result<List<Pet>> findCatList(@RequestBody Pet pet) {

        try {
            return new Result<List<Pet>>(200, "成功", petService.findPetList(pet));
        } catch (Exception e) {
            e.printStackTrace();
            return new Result<List<Pet>>(500, "失败");

        }
    }

}
