package com.pet.info.service;

import com.pet.common.dto.Pet;

import java.util.List;

public interface PetSearchService {
    List<Pet> findPetList(Pet pet);

}
