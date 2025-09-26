package com.arenabast.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminDashboardSummaryDto {
    private int totalAdmins;
    private int activeAdmins;
    private int suspendedAdmins;
    private double totalWalletBalance;
    private double averageWalletBalance;
    private long totalAgents;
    private List<TopAdminWalletDto> topAdminsByWallet;
    private List<TopAgentsWalletDto> topAgentsByWallet;
    private Long totalPlayers;
    private Map<String, WalletBalanceDto> balance;
}