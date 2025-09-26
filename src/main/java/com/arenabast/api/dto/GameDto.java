package com.arenabast.api.dto;

import lombok.Data;

import java.util.List;

@Data
public class GameDto {
    private String id;
    private String sport_key;
    private String sport_title;
    private String commence_time;
    private Boolean completed;
    private String home_team;
    private String away_team;
    private List<ScoreDto> scores;
    private String last_update;
}