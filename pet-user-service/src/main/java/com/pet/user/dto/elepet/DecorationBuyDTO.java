package com.pet.user.dto.elepet;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class DecorationBuyDTO {
    @NotBlank
    private String type;
    @NotBlank
    private String itemId;
}
