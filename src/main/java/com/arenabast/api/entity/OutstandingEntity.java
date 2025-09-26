package com.arenabast.api.entity;

import com.arenabast.api.entity.base.BaseFieldsEntity;
import com.arenabast.api.enums.RoleTypes;
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
public class OutstandingEntity extends BaseFieldsEntity {
    Long fromUserId;
    Long toUserId;
    @Enumerated(EnumType.STRING)
    RoleTypes roleType;

    Double amount;

    @Enumerated(EnumType.STRING)
    TransactionApprovalStatus status;

    String note;

    @Temporal(TemporalType.TIMESTAMP)
    Date resolvedAt;
}