package com.arenabast.api.entity;

import com.arenabast.api.entity.base.BaseFieldsEntity;
import com.arenabast.api.enums.TransactionType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TransactionLogEntity extends BaseFieldsEntity {
    Long customerId;
    Long agentId;
    Long adminId;
    Long superAdminId;

    Double betAmount;
    Boolean isWin;
    Double winAmount;

    @Enumerated(EnumType.STRING)
    TransactionType transactionType;

    Double agentCommission;
    Double adminCommission;
    Double superAdminCommission;

    Boolean isWinPayoutClaimed;

    Boolean isAgentSettled;
    Boolean isAdminSettled;
    Boolean isSuperAdminSettled;

    Date agentSettledAt;
    Date adminSettledAt;
    Date superAdminSettledAt;

    String transactionId;

    String explanation;

    Double previousBalance;
    Double currentBalance;

}