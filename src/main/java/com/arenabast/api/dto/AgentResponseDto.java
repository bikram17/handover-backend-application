package com.arenabast.api.dto;

import com.arenabast.api.enums.RoleTypes;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AgentResponseDto {
    private Long id;
    private String name;
    private String userName;
    private String email;
    private boolean isActive;
    private RoleTypes roleType;
    private Date onboardedDate;
    private Long AdminId;
    private String AdminName;
    private Double walletBalance;
    private int playerCount;
}
