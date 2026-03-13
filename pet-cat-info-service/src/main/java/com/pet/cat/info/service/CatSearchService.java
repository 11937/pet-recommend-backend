package com.pet.cat.info.service;

import com.pet.common.dto.Cat;

import java.util.List;

public interface CatSearchService {
    List<Cat> findCatList(Cat catBreed);

}
