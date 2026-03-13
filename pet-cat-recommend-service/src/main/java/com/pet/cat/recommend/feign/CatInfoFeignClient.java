package com.pet.cat.recommend.feign;

import com.pet.common.dto.Cat;
import com.pet.common.entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

// name = 你要调用的 服务名
@FeignClient(
        name = "pet-info-search",
        url = "http://localhost:1010"  // Feign 默认调用HTTP
    )
public interface CatInfoFeignClient {

    // 这里写对方服务的完整接口地址
    @GetMapping("/breed/allCats")
    Result<List<Cat>> findAllCatList();
}