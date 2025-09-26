package com.arenabast.api.entity;

import com.arenabast.api.entity.base.BaseFieldsEntity;
import com.arenabast.api.enums.TransactionApprovalStatus;
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
public class ReimbursementRequestEntity extends BaseFieldsEntity {
    Long agentId;
    Long superAdminId;

    Long transactionLogId;

    Double requestedAmount;

    String reasonNote;

    @Enumerated(EnumType.STRING)
    TransactionApprovalStatus status;

    @Temporal(TemporalType.TIMESTAMP)
    Date approvedAt;
}