package com.pet.cat.info.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pet.common.dto.Cat;
import org.apache.ibatis.annotations.Mapper;


import java.util.List;

@Mapper
public interface AllCatsMapper extends BaseMapper {
    List<Cat> findAllCatList();
}
