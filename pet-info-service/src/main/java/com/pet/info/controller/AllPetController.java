package com.pet.info.controller;

import com.pet.common.dto.Pet;
import com.pet.common.entity.Result;
import com.pet.info.service.AllPetInfoService;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController  //声明控制器 响应请求
@RequestMapping("/breed")  //请求映射父路径
public class AllPetController {

    @Resource
    private AllPetInfoService allPetList;

    @GetMapping("/allPets")
    public Result<List<Pet>> findAllPetsList(){
        try {
            return new Result<List<Pet>>(200, "成功", allPetList.findAllPetList());
        } catch (Exception e) {
            e.printStackTrace();
            return new Result<List<Pet>>(500, "失败");
        }
    }
}
