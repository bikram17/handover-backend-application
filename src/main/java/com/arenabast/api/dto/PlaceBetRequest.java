package com.arenabast.api.dto;

import lombok.Data;

@Data
public class PlaceBetRequest {
    private Double stake;       // amount player wants to bet
    private String sportKey;    // e.g. "soccer_epl"
    private String eventId;     // match id
    private String market;      // e.g. "h2h"
    private String selection;   // e.g. "Arsenal"
    private Double odds;        // e.g. 1.95
    private String betTeam;
}