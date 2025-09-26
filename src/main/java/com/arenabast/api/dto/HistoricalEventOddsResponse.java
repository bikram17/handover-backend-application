package com.arenabast.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HistoricalEventOddsResponse {
    private String timestamp;
    private String previous_timestamp;
    private String next_timestamp;
    private EventOddsDto data;
}
