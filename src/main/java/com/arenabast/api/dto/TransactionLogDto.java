package com.arenabast.api.dto;

import com.arenabast.api.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionLogDto {
    Date agentSettledAt;
    Date adminSettledAt;
    Date superAdminSettledAt;
    private String transactionId;
    private Long customerId;
    private Long agentId;
    private Long adminId;
    private Boolean isWin;
    private Double betAmount;
    private Double winAmount;
    private Double agentCommission;
    private Double adminCommission;
    private Double superAdminCommission;
    private String explanation;
    private boolean isAgentSettled;
    private boolean isAdminSettled;
    private boolean isSuperAdminSettled;
    private TransactionType transactionType;
    private Long superAdminId;
    private Date createdAt;
    private Double currentBalance;
    private Double previousBalance;
}
