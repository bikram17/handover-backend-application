package com.arenabast.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlayerDto {
    private Long id;
    private String name;
    private String userName;
    private String email;
    private String phone;
    private String address;
    private Long agentId;
    private String agentName;
    private Long adminId;
    private String adminName;
    private String playerId;
    private boolean active;
    private Date dateOfBirth;
    // player created date
    private Date onboardedDate;
    // TODO
    private Date playerLastLoginDate;
}