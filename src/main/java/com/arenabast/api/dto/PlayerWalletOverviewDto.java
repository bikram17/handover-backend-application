package com.arenabast.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlayerWalletOverviewDto extends ResponseDto {
    private Long playerId;
    private String playerName;
    private Double cashBalance;      // Deposited funds
    private Double virtualBalance;   // Winnings
    private Double totalBalance;     // cash + virtual
    private Double totalAddedByAgent;
    private Double totalWon;
    private Double totalLost;
    private List<TransactionLogDto> recentTransactions;
    private List<WalletActivityDto> topActivities;
}