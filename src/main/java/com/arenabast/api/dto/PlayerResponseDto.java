package com.arenabast.api.dto;

import lombok.Data;

@Data
public class PlayerResponseDto {
    private Long id;
    private String name;
    private String username;
    private WalletBalanceDto wallet;
}
