package com.pet.user.dto.elepet;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
public class PetProfileCreateDTO {
    @NotBlank
    private String name;
    @NotNull
    private Long breedId;
    private LocalDate birthday;

    private String Avatar;

    private String category;

}
