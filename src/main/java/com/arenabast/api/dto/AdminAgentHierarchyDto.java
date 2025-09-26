package com.arenabast.api.dto;

import lombok.Data;

import java.util.List;

@Data
public class AdminAgentHierarchyDto {
    private Long adminId;
    private String adminName;
    private List<AgentBasicDto> agents;
}