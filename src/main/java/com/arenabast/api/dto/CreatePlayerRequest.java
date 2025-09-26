package com.arenabast.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreatePlayerRequest {
    @NotBlank
    private String name;
    @NotBlank
    private String userName;
    @Email
    private String email;
    @NotBlank
    private String password;
    private String phone;
    private String address;
    private String playerId;
    private String dateOfBirth;
    private Long agentId;
    private Long adminId;
}