package com.arenabast.api.controller;

import com.arenabast.api.dao.AdminDao;
import com.arenabast.api.dao.AgentDao;
import com.arenabast.api.dto.ResponseWrapper;
import com.arenabast.api.entity.AgentEntity;
import com.arenabast.api.service.OnboardingService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/public")
public class PublicController extends ApiRestHandler {

    private final AdminDao adminDao;
    private final PasswordEncoder passwordEncoder;
    private final AgentDao agentDao;
    private final OnboardingService onboardingService;

    public PublicController(AdminDao adminDao, PasswordEncoder passwordEncoder, AgentDao agentDao, OnboardingService onboardingService) {
        this.adminDao = adminDao;
        this.passwordEncoder = passwordEncoder;
        this.agentDao = agentDao;
        this.onboardingService = onboardingService;
    }

    @GetMapping("/hash-password")
    public void hashPassword() {
        List<AgentEntity> admins = agentDao.findAll();
        admins.forEach(adminEntity -> {
            String hashedPassword = passwordEncoder.encode(adminEntity.getPasswordHash());
            adminEntity.setPasswordHash(hashedPassword);
        });

        agentDao.saveAll(admins);
    }

    @GetMapping("/backfill/wallets")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseWrapper<String> backfillManagementWallets() {
        onboardingService.backfillWalletsForAdminsAndAgents();
        return new ResponseWrapper<>(200, "Wallets created for all users missing one", null);
    }
}
