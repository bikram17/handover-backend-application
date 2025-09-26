package com.arenabast.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SettlementRequestsSummaryDto {
    private long approvedCount;
    private double approvedAmount;

    private long rejectedCount;
    private double rejectedAmount;

    private long pendingCount;
    private double pendingAmount;

    private List<SettlementRequestDto> requests;
}
