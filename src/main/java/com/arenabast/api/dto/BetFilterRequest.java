package com.arenabast.api.dto;

import com.arenabast.api.enums.BetResult;
import com.arenabast.api.enums.BetStatus;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
public class BetFilterRequest {
    private Long playerId;
    private BetStatus status;
    private BetResult result;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Date fromDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Date toDate;

    private String eventId;
    private String sportKey;
}