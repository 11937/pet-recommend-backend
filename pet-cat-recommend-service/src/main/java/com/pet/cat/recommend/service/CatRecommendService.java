package com.pet.cat.recommend.service;


import com.pet.common.dto.Cat;
import com.pet.common.dto.CatRecommendDTO;

import java.util.List;

public interface CatRecommendService {
    List<Cat> recommendCats(CatRecommendDTO dto);
}
