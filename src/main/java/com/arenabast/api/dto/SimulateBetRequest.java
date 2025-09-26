package com.arenabast.api.dto;

import lombok.Data;

@Data
public class SimulateBetRequest {
    private Long playerId;
    private Double amount;
    private Boolean isWin;
}