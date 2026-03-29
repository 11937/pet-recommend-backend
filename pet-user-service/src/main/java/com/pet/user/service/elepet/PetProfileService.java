package com.pet.user.service.elepet;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pet.user.dto.elepet.PetProfileCreateDTO;
import com.pet.user.entity.elepet.PetProfile;

public interface PetProfileService extends IService<PetProfile> {
    PetProfile createPet(Long userId, PetProfileCreateDTO dto);
}
