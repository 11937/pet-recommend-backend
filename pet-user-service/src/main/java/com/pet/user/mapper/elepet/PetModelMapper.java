package com.pet.user.mapper.elepet;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pet.user.entity.elepet.PetModel;

import java.util.List;

public interface PetModelMapper extends BaseMapper<PetModel> {
    @Override
    List<PetModel> selectList(Wrapper<PetModel> queryWrapper);
}