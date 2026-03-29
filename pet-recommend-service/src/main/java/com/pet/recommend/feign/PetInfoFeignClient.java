package com.pet.recommend.feign;

import com.pet.common.dto.Pet;
import com.pet.common.entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

// name = 要调用的服务名
@FeignClient(
        name = "info-service",
        url = "http://localhost:1010"  // Feign 默认调用HTTP
    )
public interface PetInfoFeignClient {

    // 这里写对方服务的完整接口地址
    @GetMapping("/breed/allPets")
    Result<List<Pet>> findAllPetsList();
}