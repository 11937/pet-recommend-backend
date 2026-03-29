package com.pet.user.service.Impl.elepet;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pet.user.entity.elepet.PetModel;
import com.pet.user.mapper.elepet.PetModelMapper;
import com.pet.user.service.elepet.PetModelService;
import org.springframework.stereotype.Service;

@Service
public class PetModelServiceImpl extends ServiceImpl<PetModelMapper, PetModel> implements PetModelService {
}
