package com.pet.user.controller.elepet;

import com.pet.common.entity.Result;
import com.pet.user.entity.elepet.PetModel;
import com.pet.user.service.elepet.PetModelService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/elePet/model")
public class PetModelController {

    @Resource
    private PetModelService petModelService;

    @GetMapping("/categories")
    public Result<List<PetModel>> getCategories() {
        List<PetModel> list = petModelService.list();
        return new Result<>(200, "查询成功", list);
    }
}