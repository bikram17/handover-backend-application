package com.arenabast.api.dto;

import lombok.Data;

@Data
public class ScoreDto {
    private String name;   // team name
    private String score;  // score as String in API (e.g. "113")
}
