package com.arenabast.api.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AgentDto {
    String agentName;
    Date agentOnboardingDate;
    String agentOnboardingStatus;
    Long agentId;
    String agentEmail;
    String reporterId;
    String reporterName;
}
