package com.arenabast.api.dto;

import lombok.Data;

import java.util.List;

@Data
public class MarketDto {
    private String key;
    private List<OutcomeDto> outcomes;
}