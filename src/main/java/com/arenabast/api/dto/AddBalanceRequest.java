package com.arenabast.api.dto;

import com.arenabast.api.enums.RoleTypes;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddBalanceRequest {
    private Long userId;
    private Double amount;
    private String note;
    private RoleTypes userRole;
}