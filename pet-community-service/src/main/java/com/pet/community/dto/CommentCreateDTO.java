package com.pet.community.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class CommentCreateDTO {

    @NotBlank
    @Size(max = 500)
    private String content;
}
