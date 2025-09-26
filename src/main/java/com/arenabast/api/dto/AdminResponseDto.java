package com.arenabast.api.dto;

import com.arenabast.api.enums.RoleTypes;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminResponseDto {
    private Long id;
    private String name;
    private String username;
    private String email;
    private boolean isActive;
    private RoleTypes roleType;
    private Date onboardedDate;
    private Double walletBalance;
    private int assignedAgentCount;
}
