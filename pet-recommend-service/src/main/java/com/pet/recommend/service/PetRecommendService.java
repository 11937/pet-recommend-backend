package com.pet.recommend.service;


import com.pet.common.dto.Pet;
import com.pet.common.dto.PetRecommendDTO;

import java.util.List;

public interface PetRecommendService {
    List<Pet> recommendPets(PetRecommendDTO dto);
}
