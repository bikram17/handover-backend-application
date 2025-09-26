package com.arenabast.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminEditRequestDto {
    private String name;
    private String email;
    private String password;
    private Boolean active; // Optional status toggle here
}
