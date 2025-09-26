package com.arenabast.api.entity;

import com.arenabast.api.entity.base.BaseFieldsEntity;
import com.arenabast.api.enums.BetResult;
import com.arenabast.api.enums.BetStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;


@Entity
@Data
public class BetEntity extends BaseFieldsEntity {
    private Long playerId;
    private String eventId;
    private String sportKey;
    private String market;
    private String selection;
    private Double odds;
    private Double stake;
    private Double potentialWin;

    private String betTeam;

    @Enumerated(EnumType.STRING)
    private BetStatus status; // PENDING, WON, LOST, CANCELLED

    @Enumerated(EnumType.STRING)
    private BetResult result; // WIN, LOSS, VOID

    @Temporal(TemporalType.TIMESTAMP)
    private Date placedAt;
    @Temporal(TemporalType.TIMESTAMP)
    private Date settledAt;
}