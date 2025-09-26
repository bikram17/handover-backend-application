package com.arenabast.api.dto;

import com.arenabast.api.enums.TransactionType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WalletDto {
    BigInteger amount;
    String description;
    @Enumerated(EnumType.STRING)
    TransactionType transactionType;
}
