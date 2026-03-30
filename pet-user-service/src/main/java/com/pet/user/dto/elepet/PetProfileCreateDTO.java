package com.pet.user.dto.elepet;

import lombok.Data;
import javax.validation.constraints.NotBlank;

import java.time.LocalDate;

@Data
public class PetProfileCreateDTO {
    @NotBlank
    private String name;
    private Long breedId;

    private LocalDate birthday;
    private String Avatar;
    private String category;

}
