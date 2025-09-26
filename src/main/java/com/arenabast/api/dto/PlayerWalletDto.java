package com.arenabast.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlayerWalletDto {
    private Double cashBalance;
    private Double virtualBalance;
    private Double totalAddedByAgent;
    private Double totalWon;
    private Double totalLost;
}
