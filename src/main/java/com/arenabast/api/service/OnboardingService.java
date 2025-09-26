package com.arenabast.api.service;

import com.arenabast.api.auth.jwt.UserContext;
import com.arenabast.api.dao.AdminDao;
import com.arenabast.api.dao.AgentDao;
import com.arenabast.api.dao.ManagementWalletDao;
import com.arenabast.api.dao.SuperAdminDao;
import com.arenabast.api.dto.UserAddRequestDto;
import com.arenabast.api.entity.AdminEntity;
import com.arenabast.api.entity.AgentEntity;
import com.arenabast.api.entity.ManagementWalletEntity;
import com.arenabast.api.entity.SuperAdminEntity;
import com.arenabast.api.enums.RoleTypes;
import com.arenabast.api.exception.DataValidationException;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OnboardingService {

    private final AdminDao adminDao;
    private final UserContext userContext;
    private final AgentDao agentDao;
    private final PasswordEncoder passwordEncoder;
    private final ManagementWalletDao managementWalletDao;
    private final SuperAdminDao superAdminDao;

    public OnboardingService(AdminDao adminDao, UserContext userContext, AgentDao agentDao, PasswordEncoder passwordEncoder, ManagementWalletDao managementWalletDao, SuperAdminDao superAdminDao) {
        this.adminDao = adminDao;
        this.userContext = userContext;
        this.agentDao = agentDao;
        this.passwordEncoder = passwordEncoder;
        this.managementWalletDao = managementWalletDao;
        this.superAdminDao = superAdminDao;
    }

    public void addUser(@Valid UserAddRequestDto userAddRequestDto) {
        RoleTypes role = userAddRequestDto.getRole();
        Optional<AdminEntity> optAdminEntity = adminDao.findByUserName(userAddRequestDto.getUserName());
        Optional<AgentEntity> optAgentEntity = agentDao.findByUserName(userAddRequestDto.getUserName());
        if (optAdminEntity.isPresent() || optAgentEntity.isPresent()) {
            throw new DataValidationException("user already exists");
        }
        switch (role) {
            case ADMIN -> {
                AdminEntity adminEntity = new AdminEntity();
                adminEntity.setEmail(userAddRequestDto.getEmail());
                adminEntity.setPasswordHash(passwordEncoder.encode(userAddRequestDto.getPassword()));
                adminEntity.setName(userAddRequestDto.getName());
                adminEntity.setCreatedBy(userAddRequestDto.getUserName());
                adminEntity.setUserName(userAddRequestDto.getUserName());
                adminEntity.setActive(true);
                adminEntity = adminDao.save(adminEntity);

                // Create wallet for ADMIN
                createWalletIfAbsent(adminEntity.getId(), RoleTypes.ADMIN);
            }

            case AGENT -> {
                AgentEntity agentEntity = new AgentEntity();
                agentEntity.setEmail(userAddRequestDto.getEmail());
                agentEntity.setPasswordHash(passwordEncoder.encode(userAddRequestDto.getPassword()));
                agentEntity.setName(userAddRequestDto.getName());
                agentEntity.setUserName(userAddRequestDto.getUserName());
                agentEntity.setCreatedBy(UserContext.getUsername());
                agentEntity.setActive(true);

                String creatorRole = UserContext.getRole();
                Long adminId;

                if ("SUPER_ADMIN".equals(creatorRole)) {
                    adminId = userAddRequestDto.getAdminId();
                    if (adminId == null) throw new DataValidationException("Admin ID is required");
                } else if ("ADMIN".equals(creatorRole)) {
                    adminId = userContext.getUserId();
                } else {
                    throw new DataValidationException("Unauthorized role");
                }

                adminDao.findById(adminId).orElseThrow(() -> new DataValidationException("Admin not found"));
                agentEntity.setAdminId(adminId);
                agentEntity = agentDao.save(agentEntity);

                // Create wallet for AGENT
                createWalletIfAbsent(agentEntity.getId(), RoleTypes.AGENT);
            }

            default -> throw new DataValidationException("Invalid role type");
        }
    }

    private void createWalletIfAbsent(Long userId, RoleTypes role) {
        boolean exists = managementWalletDao.findByUserIdAndRole(userId, role).isPresent();
        if (!exists) {
            ManagementWalletEntity wallet = new ManagementWalletEntity();
            wallet.setUserId(userId);
            wallet.setRole(role);
            wallet.setBalance(0.0);
            wallet.setTotalReceived(0.0);
            wallet.setTotalSettled(0.0);
            managementWalletDao.save(wallet);
        }
    }

    public void backfillWalletsForAdminsAndAgents() {
        List<AdminEntity> admins = adminDao.findAll();
        List<AgentEntity> agents = agentDao.findAll();
        List<SuperAdminEntity> superAdmins = superAdminDao.findAll();

        admins.forEach(admin -> createWalletIfAbsent(admin.getId(), RoleTypes.ADMIN));
        agents.forEach(agent -> createWalletIfAbsent(agent.getId(), RoleTypes.AGENT));
        superAdmins.forEach(sa -> createWalletIfAbsent(sa.getId(), RoleTypes.SUPER_ADMIN));
    }
}