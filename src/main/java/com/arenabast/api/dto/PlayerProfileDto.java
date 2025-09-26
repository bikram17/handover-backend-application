package com.arenabast.api.dto;

import lombok.Data;

import java.util.List;

@Data
public class PlayerProfileDto {
    private PlayerDto player;
    private PlayerWalletDto wallet;
    private List<TransactionLogDto> recentTransactions;
    // Optional: private List<GameDto> recentGames;
}