package com.arenabast.api.dto;

import lombok.Data;

@Data
public class LoginResponseDto {
    private String token;
    private PlayerResponseDto player;
}
