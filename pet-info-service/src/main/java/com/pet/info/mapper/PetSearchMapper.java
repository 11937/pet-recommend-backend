package com.pet.info.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.pet.common.dto.Pet;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PetSearchMapper extends BaseMapper<Pet> {

    List<Pet> findPetList(Pet pet);


}
