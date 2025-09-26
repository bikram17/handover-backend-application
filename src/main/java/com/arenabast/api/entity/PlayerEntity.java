package com.arenabast.api.entity;

import com.arenabast.api.entity.base.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Builder
@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
public class PlayerEntity extends BaseEntity {
    Long agentId;

    String phone;
    String address;
    private String playerId;
    @Temporal(TemporalType.DATE)
    private Date dateOfBirth;
    @Builder.Default
    boolean walletCreated = false;

}