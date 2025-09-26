package com.arenabast.api.entity.base;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigInteger;

@MappedSuperclass
@Data
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@AllArgsConstructor
public class WalletBalanceBaseEntity extends BaseFieldsEntity {
    BigInteger balance;
    BigInteger total;

    BigInteger available;

    BigInteger virtualCash;
    BigInteger lockedCash;
    BigInteger dues;
    BigInteger loanedAmount;
}
