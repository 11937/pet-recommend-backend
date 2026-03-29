package com.pet.info.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pet.common.dto.Pet;
import org.apache.ibatis.annotations.Mapper;


import java.util.List;

@Mapper
public interface AllPetsMapper extends BaseMapper {
    List<Pet> findAllPetList();
}
