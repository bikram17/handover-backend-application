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
public class WalletEntity extends BaseFieldsEntity {
    Long playerId;

    Double cashBalance;
    Double virtualBalance;

    Double totalAddedByAgent;
    Double totalWon;
    Double totalLost;

    @Temporal(TemporalType.TIMESTAMP)
    Date lastPlayedAt;
}