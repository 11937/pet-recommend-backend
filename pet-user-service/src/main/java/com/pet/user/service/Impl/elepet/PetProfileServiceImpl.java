package com.pet.user.service.Impl.elepet;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pet.user.dto.elepet.PetProfileCreateDTO;
import com.pet.user.entity.elepet.PetProfile;
import com.pet.user.mapper.elepet.PetProfileMapper;
import com.pet.user.service.elepet.PetProfileService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PetProfileServiceImpl extends ServiceImpl<PetProfileMapper, PetProfile> implements PetProfileService {

    @Override
    @Transactional
    public PetProfile createPet(Long userId, PetProfileCreateDTO dto) {
        PetProfile pet = new PetProfile();
        pet.setUserId(userId);
        pet.setName(dto.getName());
        pet.setBreedId(dto.getBreedId());
        pet.setAvatar(dto.getAvatar());
        pet.setBirthday(dto.getBirthday());
        pet.setCategory(dto.getCategory());
        // 初始 decoration 为 null
        save(pet);
        return pet;
    }
}
