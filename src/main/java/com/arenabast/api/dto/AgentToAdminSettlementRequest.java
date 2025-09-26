package com.arenabast.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AgentToAdminSettlementRequest {
    private Double amount;
    private String note;
}