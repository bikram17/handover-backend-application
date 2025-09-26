package com.arenabast.api.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TopAdminWalletDto {
    private Long adminId;
    private String adminName;
    private double walletBalance;
}

