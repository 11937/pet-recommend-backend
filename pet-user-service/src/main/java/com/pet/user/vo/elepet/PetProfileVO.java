package com.pet.user.vo.elepet;

import com.pet.user.entity.elepet.PetProfile;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class PetProfileVO {
    private Long id;
    private String name;
    private Long breedId;
    private String avatar;
    private LocalDate birthday;
    private String decoration;
    private LocalDateTime createdAt;

    public static PetProfileVO from(PetProfile pet) {
        PetProfileVO vo = new PetProfileVO();
        vo.setId(pet.getId());
        vo.setName(pet.getName());
        vo.setBreedId(pet.getBreedId());
        vo.setAvatar(pet.getAvatar());
        vo.setBirthday(pet.getBirthday());
        vo.setDecoration(pet.getDecoration());
        vo.setCreatedAt(pet.getCreatedAt());
        return vo;
    }
}
