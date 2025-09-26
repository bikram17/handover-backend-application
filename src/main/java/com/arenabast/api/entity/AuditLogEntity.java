package com.arenabast.api.entity;

import com.arenabast.api.entity.base.BaseFieldsEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuditLogEntity extends BaseFieldsEntity {
    Long actorUserId;

    String actionType; // e.g. "APPROVED_OUTSTANDING", "TXN_CREATED"
    String entity;     // "TransactionLogEntity", "WalletEntity", etc.
    Long entityId;

    String oldValue;
    String newValue;
    String description;

    @Temporal(TemporalType.TIMESTAMP)
    Date timestamp;
}