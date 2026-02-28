package com.cat_card.service;

import com.cat_card.pojo.Cat;
import com.cat_card.pojo.CatRecommendDTO;

import java.util.List;

public interface CatRecommendService {
    List<Cat> recommendCats(CatRecommendDTO dto);
}
