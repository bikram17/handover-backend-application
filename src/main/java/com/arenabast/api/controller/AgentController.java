package com.arenabast.api.controller;

import com.arenabast.api.dto.*;
import com.arenabast.api.service.AdminService;
import com.arenabast.api.service.AgentService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/agent")
@RequiredArgsConstructor
public class AgentController extends ApiRestHandler {

    private final AgentService agentService;
    private final AdminService adminService;

    @GetMapping("/all")
    public ResponseWrapper<AgentDto> getAllAgents() {
        return null;
    }

    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN, ADMIN, AGENT')")
    @PostMapping("/wallet/approve/{requestId}")
    public ResponseEntity<Void> approveWalletRequest(@PathVariable Long requestId) {
        agentService.approveWalletRequest(requestId);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'ADMIN')")
    @PutMapping("/agent/edit/{agentId}")
    public ResponseWrapper<String> editAgent(
            @PathVariable Long agentId,
            @RequestBody AgentEditRequestDto requestDto) {
        agentService.updateAgent(agentId, requestDto);
        return new ResponseWrapper<>(true, 200, "Agent updated successfully");
    }

    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'ADMIN')")
    @GetMapping("/agent")
    public ResponseWrapper<PaginatedResponse<AgentResponseDto>> listAgent(
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date endDate,
            @RequestParam(required = false) Long adminId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PaginatedResponse<AgentResponseDto> result = adminService.getAllAgents(active, name, startDate, endDate, page, size, adminId);
        return new ResponseWrapper<>(true, 200, result);
    }


    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'ADMIN')")
    @PutMapping("/agent/status/{agentId}")
    public ResponseWrapper<String> toggleAgentStatus(
            @PathVariable Long agentId,
            @RequestParam boolean active) {
        agentService.changeAgentStatus(agentId, active);
        return new ResponseWrapper<>(true, 200, "Agent status updated");
    }

    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'ADMIN')")
    @GetMapping("/agent/{id}")
    public ResponseWrapper<AgentResponseDto> getAgentById(@PathVariable Long id) {
        return new ResponseWrapper<>(true, 200, adminService.getAgentById(id));
    }

    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'ADMIN')")
    @GetMapping("/agent/export")
    public ResponseEntity<byte[]> exportAgentsAsCsv(
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date endDate) {

        // Get all agents without pagination for export (role-based filter inside service)
        List<AgentResponseDto> agents = adminService.getAllAgentsForExport(active, name, startDate, endDate);

        // Convert to CSV
        StringBuilder csvBuilder = new StringBuilder();
        csvBuilder.append("ID,Name,Email,Active,Role,Onboarded Date,Wallet Balance,Admin ID,Player Count,Admin Name\n");

        for (AgentResponseDto agent : agents) {
            csvBuilder.append(agent.getId()).append(",");
            csvBuilder.append(escapeCsv(agent.getName())).append(",");
            csvBuilder.append(escapeCsv(agent.getEmail())).append(",");
            csvBuilder.append(agent.isActive()).append(",");
            csvBuilder.append(agent.getRoleType()).append(",");
            csvBuilder.append(agent.getOnboardedDate()).append(",");
            csvBuilder.append(agent.getWalletBalance()).append(",");
            csvBuilder.append(agent.getAdminId()).append(",");
            csvBuilder.append(agent.getPlayerCount()).append("\n");
            csvBuilder.append(escapeCsv(agent.getAdminName())).append("\n");
        }

        byte[] csvBytes = csvBuilder.toString().getBytes(StandardCharsets.UTF_8);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.setContentDisposition(ContentDisposition.builder("attachment")
                .filename("agents_export.csv")
                .build());

        return ResponseEntity.ok()
                .headers(headers)
                .body(csvBytes);
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        String escaped = value.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }


}
