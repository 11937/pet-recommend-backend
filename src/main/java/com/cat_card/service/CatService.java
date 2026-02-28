package com.cat_card.service;

import com.cat_card.pojo.Cat;

import java.util.List;

public interface CatService {

    List<Cat> findCatList(Cat catBreed);

}
