package com.arenabast.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SportCategoryDto {
    private Long sport_id;
    private String sport_name;
    private String sport_code;
    private String icon_url;
    private Integer display_order;
    private Boolean is_featured;
    private Integer active_events_count;

    private List<LeagueDto> leagues;
}
