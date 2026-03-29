package com.pet.recommend.feign;

import com.pet.common.entity.Result;

import com.pet.recommend.DTO.UserMatchSaveDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
        name = "user-service",
        url = "http://localhost:1012"
    )
public interface UserMatchFeignClient {

    @PostMapping("/user/match")
    Result<Void> saveMatch(@RequestBody UserMatchSaveDTO dto,
                           @RequestHeader("X-User-Id") Long userId);
}