package com.arenabast.api.dto;

import com.arenabast.api.entity.base.BaseFieldsEntity;
import com.arenabast.api.enums.TransactionApprovalStatus;
import com.arenabast.api.enums.TransactionType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WalletTransactionRequestEntity extends BaseFieldsEntity {

    Long playerId;
    Long agentId;

    @Enumerated(EnumType.STRING)
    TransactionType transactionType; // DEPOSIT or WITHDRAW

    Double requestedAmount;

    @Enumerated(EnumType.STRING)
    TransactionApprovalStatus status; // PENDING, APPROVED, REJECTED

    String note;

    Double lockedCashAmount; // only for withdrawals

    @Temporal(TemporalType.TIMESTAMP)
    Date resolvedAt;
}