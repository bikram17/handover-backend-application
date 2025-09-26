package com.arenabast.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TopAgentsWalletDto {
    private Long agentId;
    private String agentName;
    private double walletBalance;
}
