package com.arenabast.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AgentEditRequestDto {
    private String name;
    private String email;
    private String password;
    private Boolean active;
}
