package com.arenabast.api.dto;

import com.arenabast.api.enums.RoleTypes;
import com.arenabast.api.enums.TransactionApprovalStatus;
import lombok.Data;

import java.util.Date;

@Data
public class SettlementRequestDto {
    private Long requestId;
    private Long fromUserId;
    private String fromUserName;
    private RoleTypes fromUserRole;
    private Long id;

    private Long toUserId;
    private String toUserName;
    private RoleTypes toUserRole;

    private Double amount;
    private String note;
    private TransactionApprovalStatus status;
    private Date createdAt;
    private Date resolvedAt;
}