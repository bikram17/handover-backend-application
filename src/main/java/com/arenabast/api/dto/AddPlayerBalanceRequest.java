package com.arenabast.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddPlayerBalanceRequest {
    private Long playerId;
    private Double amount;
    private String note; // optional, for logging/audit if needed
}