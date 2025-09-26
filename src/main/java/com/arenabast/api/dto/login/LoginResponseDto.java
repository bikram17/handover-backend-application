package com.arenabast.api.dto.login;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponseDto {
    private String role;
    private Long id;
    private String accessToken;
    private String email;
    private String userName;
}