package com.arenabast.api.dto;

import com.arenabast.api.enums.RoleTypes;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WalletOverviewDto extends ResponseDto {
    List<OwedPartyDto> peopleIOwe;
    List<OwedPartyDto> peopleWhoOweMe;
    private Long userId;
    private String userName;
    private RoleTypes role;
    private Double currentBalance;
    private Double virtualBalance;
    private Double totalReceived;
    private Double totalSettled;
    private Double balanceOwed;
    private Double usableBalance;
    private List<TransactionLogDto> recentTransactions;
    private List<SettlementRequestDto> recentRequests;
    private List<WalletActivityDto> topActivities;
}