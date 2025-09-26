package com.arenabast.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdatePlayerRequest {
    private String name;
    private String phone;
    private String address;
    private String dateOfBirth; // format: yyyy-MM-dd
    private Boolean active;     // optional
    private String password;
}