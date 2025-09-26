package com.arenabast.api.dto;

import lombok.Data;

import java.util.List;

@Data
public class EventDto {
    private String id;
    private String sport_key;
    private String sport_title;
    private String commence_time;
    private String home_team;
    private String away_team;
    private List<BookmakerDto> bookmakers;
}