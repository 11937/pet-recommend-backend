package com.pet.user.dto.elepet;

import lombok.Data;

import java.util.Map;

@Data
public class DecorationUpdateDTO {
    private Map<String, Object> decoration; // 前端传JSON对象，后端存JSON字符串
}
