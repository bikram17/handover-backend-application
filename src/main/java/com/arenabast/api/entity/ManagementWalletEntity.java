package com.arenabast.api.entity;

import com.arenabast.api.entity.base.BaseFieldsEntity;
import com.arenabast.api.enums.RoleTypes;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

import static lombok.AccessLevel.PRIVATE;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = PRIVATE)
public class ManagementWalletEntity extends BaseFieldsEntity {

    Long userId;
    @Enumerated(value = EnumType.STRING)
    RoleTypes role;

    Double balance;
    Double totalReceived;
    Double totalSettled;
}