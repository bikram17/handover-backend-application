package com.arenabast.api.dto;

import lombok.Data;

import java.util.List;

@Data
public class BookmakerDto {
    private String key;
    private String title;
    private String last_update;
    private List<MarketDto> markets;
}