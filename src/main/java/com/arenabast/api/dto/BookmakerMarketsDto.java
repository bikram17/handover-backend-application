package com.arenabast.api.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookmakerMarketsDto {
    private String key;   // bookmaker key (e.g. fanduel, draftkings)
    private String title; // bookmaker name
    private List<MarketKeyDto> markets;
}
