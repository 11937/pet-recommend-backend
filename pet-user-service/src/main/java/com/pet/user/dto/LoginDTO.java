package com.pet.user.dto;

import lombok.Data;

@Data
public class LoginDTO {
    private String phone;
    private String password;
    private String Nickname;
}