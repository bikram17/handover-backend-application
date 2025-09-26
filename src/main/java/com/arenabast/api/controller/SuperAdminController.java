package com.arenabast.api.controller;

import com.arenabast.api.auth.jwt.UserContext;
import com.arenabast.api.dao.AdminDao;
import com.arenabast.api.dao.AgentDao;
import com.arenabast.api.dto.*;
import com.arenabast.api.entity.AdminEntity;
import com.arenabast.api.exception.DataValidationException;
import com.arenabast.api.service.PlayerService;
import com.arenabast.api.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/superadmin")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'ADMIN')")
public class SuperAdminController extends ApiRestHandler {

    private final AdminDao adminDao;
    private final AgentDao agentDao;
    private final PlayerService playerService;
    private final UserContext userContext;
    private final WalletService walletService;

    @GetMapping("/admin-agent-hierarchy")
    public ResponseWrapper<List<AdminAgentHierarchyDto>> getAdminAgentHierarchy() {
        String role = UserContext.getRole();
        Long userId = userContext.getUserId();

        List<AdminAgentHierarchyDto> response;

        if ("SUPER_ADMIN".equals(role)) {
            response = adminDao.findAll().stream().map(admin -> {
                List<AgentBasicDto> agents = agentDao.findByAdminId(admin.getId()).stream()
                        .map(agent -> {
                            AgentBasicDto dto = new AgentBasicDto();
                            dto.setAgentId(agent.getId());
                            dto.setAgentName(agent.getName());
                            return dto;
                        }).toList();

                AdminAgentHierarchyDto adminDto = new AdminAgentHierarchyDto();
                adminDto.setAdminId(admin.getId());
                adminDto.setAdminName(admin.getName());
                adminDto.setAgents(agents);

                return adminDto;
            }).toList();
        } else if ("ADMIN".equals(role)) {
            AdminEntity admin = adminDao.findById(userId)
                    .orElseThrow(() -> new DataValidationException("Admin not found"));

            List<AgentBasicDto> agents = agentDao.findByAdminId(admin.getId()).stream()
                    .map(agent -> {
                        AgentBasicDto dto = new AgentBasicDto();
                        dto.setAgentId(agent.getId());
                        dto.setAgentName(agent.getName());
                        return dto;
                    }).toList();

            AdminAgentHierarchyDto adminDto = new AdminAgentHierarchyDto();
            adminDto.setAdminId(admin.getId());
            adminDto.setAdminName(admin.getName());
            adminDto.setAgents(agents);

            response = List.of(adminDto);
        } else {
            throw new DataValidationException("Unauthorized role for this operation");
        }

        return new ResponseWrapper<>(200, "Fetched Admin-Agent hierarchy", response);
    }

    @PostMapping("/player/change-agent-admin")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseWrapper<String> changeAgentAndAdminForPlayer(
            @RequestBody ChangePlayerAssignmentRequest request
    ) {
        playerService.reassignPlayerAgentAndAdminBySuperAdmin(request);
        return new ResponseWrapper<>(200, "Player's agent/admin reassigned", null);
    }

    @PostMapping("/management/self/add-cash")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseWrapper<String> adminAddSelfWallet(@RequestBody AddBalanceRequest req) {
        req.setUserId(userContext.getUserId()); // force to self
        walletService.selfTopUpWallet(req);
        return new ResponseWrapper<>(200, "Cash added to your wallet", null);
    }

    @GetMapping("hello-test-name")
    private String helloSuperAdmin(@RequestParam("name") String name) {
        return "Hello Super Admin " + name;
    }
}