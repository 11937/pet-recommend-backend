package com.pet.cat.info.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.pet.common.dto.Cat;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CatSearchMapper extends BaseMapper<Cat> {

    List<Cat> findCatList(Cat cat);


}
