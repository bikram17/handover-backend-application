package com.arenabast.api.dto;

import lombok.Data;

@Data
public class ChangePlayerAssignmentRequest {
    private Long playerId;
    private Long newAgentId;
    private Long newAdminId; // Optional, only for Super Admin
}