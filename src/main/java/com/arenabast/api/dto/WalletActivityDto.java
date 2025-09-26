package com.arenabast.api.dto;

import com.arenabast.api.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WalletActivityDto {
    private String transactionId;
    private TransactionType transactionType;
    private String summary;     // like "$100 deposited by Admin"
    private Date date;
}