package com.arenabast.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LeagueDto {
    private Long league_id;
    private String league_name;
    private String country;
    private String country_code;
    private String flag_url;
    private Integer active_matches;
    private Boolean is_featured;

}
