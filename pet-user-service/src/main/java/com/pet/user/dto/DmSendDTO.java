package com.pet.user.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class DmSendDTO {
    @NotNull
    private Long receiverId;
    @NotBlank
    private String content;
}
