package com.arenabast.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HistoricalOddsResponse {
    private String timestamp;
    private String previous_timestamp;
    private String next_timestamp;
    private List<EventOddsDto> data; // <-- reuse your normal odds DTO
}
