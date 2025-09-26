package com.arenabast.api.service;

import com.arenabast.api.auth.jwt.UserContext;
import com.arenabast.api.dao.*;
import com.arenabast.api.dto.*;
import com.arenabast.api.entity.AdminEntity;
import com.arenabast.api.entity.AgentEntity;
import com.arenabast.api.entity.SuperAdminEntity;
import com.arenabast.api.enums.RoleTypes;
import com.arenabast.api.exception.DataValidationException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class AdminService {

    @Autowired
    private AdminDao adminDao;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AgentDao agentDao;
    @Autowired
    private UserContext userContext;
    @Autowired
    private WalletService walletService;
    @Autowired
    private SuperAdminDao superAdminDao;
    @Autowired
    private PlayerDao playerDao;
    @Autowired
    private WalletDao walletDao;
    @Autowired
    private ManagementWalletDao managementWalletDao;

    public AdminResponseDto createAdmin(AdminRequestDto request) {
        if (adminDao.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Admin with this email already exists");
        }

        AdminEntity admin = new AdminEntity();
        admin.setName(request.getName());
        admin.setEmail(request.getEmail());
        admin.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        admin = adminDao.save(admin);

        return null;
    }

    public PaginatedResponse<AdminResponseDto> getAllAdmins(Boolean active, String name, Date startDate, Date endDate, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<AdminEntity> adminPage = adminDao.findAll(pageable);

        List<AdminResponseDto> list = adminPage.getContent().stream()
                .filter(admin -> active == null || admin.isActive() == active)
                .filter(admin -> name == null || admin.getName().toLowerCase().contains(name.toLowerCase()))
                .filter(admin -> {
                    if (startDate == null && endDate == null) return true;
                    Date created = admin.getCreatedAt();
                    return (startDate == null || !created.before(startDate)) &&
                            (endDate == null || !created.after(endDate));
                })
                .map(admin -> new AdminResponseDto(
                        admin.getId(),
                        admin.getName(),
                        admin.getUserName(),
                        admin.getEmail(),
                        admin.isActive(),
                        RoleTypes.ADMIN,
                        admin.getCreatedAt(),
                        walletService.getUsableBalanceForUser(admin.getId(), RoleTypes.ADMIN),
                        agentDao.countByAdminId(admin.getId())
                ))
                .toList();

        long activeCount = adminDao.countByActive(true);
        long suspendedCount = adminDao.countByActive(false);

        return new PaginatedResponse<>(list, adminPage.getTotalElements(), activeCount, suspendedCount, page, size);
    }

    public PaginatedResponse<AgentResponseDto> getAllAgents(Boolean active, String name, Date startDate, Date endDate, int page, int size, Long adminId) {
        String role = UserContext.getRole();
        Long callerId = userContext.getUserId();

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<AgentEntity> agentPage;

        if ("SUPER_ADMIN".equals(role)) {
            if (adminId == null) {
                agentPage = agentDao.findAll(pageable);
            } else {
                agentPage = agentDao.findByAdminId(adminId, pageable);
            }
        } else if ("ADMIN".equals(role)) {
            agentPage = agentDao.findByAdminId(callerId, pageable);
        } else {
            throw new DataValidationException("Unauthorized role to view agents.");
        }

        List<AgentResponseDto> list = agentPage.getContent().stream()
                // ✅ Filter by name if provided
                .filter(agent -> name == null || agent.getName().toLowerCase().contains(name.toLowerCase()))
                // ✅ Filter by status
                .filter(agent -> active == null || agent.isActive() == active)
                // ✅ Filter by date range
                .filter(agent -> {
                    Date created = agent.getCreatedAt();
                    return (startDate == null || !created.before(startDate)) &&
                            (endDate == null || !created.after(endDate));
                })
                // ✅ Map to DTO
                .map(agent -> {
                    int playerCount = playerDao.countByAgentId(agent.getId());
                    String adminName = adminDao.findFirstById(agent.getAdminId()).getName();
                    return new AgentResponseDto(
                            agent.getId(),
                            agent.getName(),
                            agent.getUserName(),
                            agent.getEmail(),
                            agent.isActive(),
                            RoleTypes.AGENT,
                            agent.getCreatedAt(),
                            agent.getAdminId(),
                            adminName,
                            walletService.getUsableBalanceForUser(agent.getId(), RoleTypes.AGENT),
                            playerCount
                    );
                })
                .toList();

        // ✅ Count totals for active/suspended
        long activeCount;
        long suspendedCount;

        if ("SUPER_ADMIN".equals(role)) {
            activeCount = agentDao.countByActive(true);
            suspendedCount = agentDao.countByActive(false);
        } else {
            activeCount = agentDao.countByAdminIdAndActive(callerId, true);
            suspendedCount = agentDao.countByAdminIdAndActive(callerId, false);
        }

        return new PaginatedResponse<>(list, agentPage.getTotalElements(), activeCount, suspendedCount, page, size);
    }

    public AdminDashboardSummaryDto getAdminDashboardSummary() {
        int totalAdmins = (int) adminDao.count();
        int activeAdmins = (int) adminDao.countByActive(true);
        int inactiveAdmins = totalAdmins - activeAdmins;

        double totalAdminWalletBalance = managementWalletDao.getTotalBalanceByRole(RoleTypes.ADMIN);
        double avgWalletBalance = totalAdmins > 0 ? totalAdminWalletBalance / totalAdmins : 0;

        long totalAgents = agentDao.count();

        List<TopAdminWalletDto> topAdmins = managementWalletDao.getTopAdmins(PageRequest.of(0, 5));
        List<TopAgentsWalletDto> topAgents = managementWalletDao.getTopAgents(PageRequest.of(0, 5));

        Long playerCount = playerDao.count();

        Map<String, WalletBalanceDto> walletBalances = new HashMap<>();
        WalletBalanceDto playerBalances = walletDao.getTotalPlayerBalances();
        walletBalances.put("players", playerBalances);

        walletBalances.put("agents", new WalletBalanceDto(
                managementWalletDao.getTotalBalanceByRole(RoleTypes.AGENT), 0.0
        ));
        walletBalances.put("admins", new WalletBalanceDto(
                managementWalletDao.getTotalBalanceByRole(RoleTypes.ADMIN), 100.00
        ));
        walletBalances.put("super_admin", new WalletBalanceDto(
                managementWalletDao.getTotalBalanceByRole(RoleTypes.SUPER_ADMIN), 0.0
        ));

        return new AdminDashboardSummaryDto(
                totalAdmins,
                activeAdmins,
                inactiveAdmins,
                totalAdminWalletBalance,
                avgWalletBalance,
                totalAgents,
                topAdmins,
                topAgents,
                playerCount,
                walletBalances
        );
    }

    public void updateAdmin(Long adminId, AdminEditRequestDto dto) {
        AdminEntity admin = adminDao.findById(adminId)
                .orElseThrow(() -> new DataValidationException("Admin not found"));

        if (dto.getName() != null) admin.setName(dto.getName());
        if (dto.getEmail() != null) admin.setEmail(dto.getEmail());
        if (dto.getPassword() != null) admin.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        if (dto.getActive() != null) admin.setActive(dto.getActive());

        adminDao.save(admin);
    }

    public void changeAdminStatus(Long adminId, boolean active) {
        try {
            AdminEntity admin = adminDao.findById(adminId)
                    .orElseThrow(() -> new DataValidationException("Admin not found"));

            admin.setActive(active);
            adminDao.save(admin);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    public AgentResponseDto getAgentById(Long id) {
        String role = UserContext.getRole();
        Long callerId = userContext.getUserId();

        AgentEntity agent = agentDao.findById(id)
                .orElseThrow(() -> new DataValidationException("Agent not found"));

        // 🔐 SUPER_ADMIN can view any agent
        // 🔐 ADMIN can only view their own agents
        if ("ADMIN".equals(role) && !agent.getAdminId().equals(callerId)) {
            throw new DataValidationException("You are not authorized to view this agent");
        }

        String adminName = adminDao.findFirstById(agent.getAdminId()).getName();
        int playerCount = playerDao.countByAgentId(agent.getId());
        return new AgentResponseDto(
                agent.getId(),
                agent.getName(),
                agent.getUserName(),
                agent.getEmail(),
                agent.isActive(),
                RoleTypes.AGENT,
                agent.getCreatedAt(),
                agent.getAdminId(),
                adminName,
                walletService.getUsableBalanceForUser(agent.getId(), RoleTypes.AGENT),
                playerCount
        );
    }

    public AdminResponseDto getAdminById(Long id) {
        String role = UserContext.getRole();

        // 🔐 Only SUPER_ADMIN can view any admin
        if (!"SUPER_ADMIN".equals(role)) {
            throw new DataValidationException("Only SUPER_ADMIN can view admin details");
        }

        AdminEntity admin = adminDao.findById(id)
                .orElseThrow(() -> new DataValidationException("Admin not found"));

        return new AdminResponseDto(
                admin.getId(),
                admin.getName(),
                admin.getUserName(),
                admin.getEmail(),
                admin.isActive(),
                RoleTypes.ADMIN,
                admin.getCreatedAt(),
                walletService.getUsableBalanceForUser(admin.getId(), RoleTypes.ADMIN),
                agentDao.countByAdminId(admin.getId())
        );
    }

    @Transactional
    public void updateOwnProfile(UpdateProfileRequest request) {
        String role = UserContext.getRole();
        Long callerId = userContext.getUserId();

        if ("SUPER_ADMIN".equals(role)) {
            SuperAdminEntity user = superAdminDao.findById(callerId)
                    .orElseThrow(() -> new DataValidationException("Super Admin not found"));

            if (request.getName() != null) user.setName(request.getName());
            if (request.getEmail() != null) user.setEmail(request.getEmail());
            if (request.getPassword() != null && !request.getPassword().isBlank()) {
                user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
            }
            superAdminDao.save(user);

        } else if ("ADMIN".equals(role)) {
            AdminEntity user = adminDao.findById(callerId)
                    .orElseThrow(() -> new DataValidationException("Admin not found"));

            if (request.getName() != null) user.setName(request.getName());
            if (request.getEmail() != null) user.setEmail(request.getEmail());
            if (request.getPassword() != null && !request.getPassword().isBlank()) {
                user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
            }
            adminDao.save(user);

        } else if ("AGENT".equals(role)) {
            AgentEntity user = agentDao.findById(callerId)
                    .orElseThrow(() -> new DataValidationException("Agent not found"));

            if (request.getName() != null) user.setName(request.getName());
            if (request.getEmail() != null) user.setEmail(request.getEmail());
            if (request.getPassword() != null && !request.getPassword().isBlank()) {
                user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
            }
            agentDao.save(user);

        } else {
            throw new DataValidationException("Unauthorized role");
        }
    }


    public UserProfileDto getOwnProfile() {
        String role = UserContext.getRole();
        Long callerId = userContext.getUserId();

        if ("SUPER_ADMIN".equals(role)) {
            SuperAdminEntity user = superAdminDao.findById(callerId)
                    .orElseThrow(() -> new DataValidationException("Super Admin not found"));
            return new UserProfileDto(user.getId(), user.getName(), user.getUserName(), user.getEmail(), RoleTypes.SUPER_ADMIN, user.isActive(), user.getCreatedAt());

        } else if ("ADMIN".equals(role)) {
            AdminEntity user = adminDao.findById(callerId)
                    .orElseThrow(() -> new DataValidationException("Admin not found"));
            return new UserProfileDto(user.getId(), user.getName(), user.getUserName(), user.getEmail(), RoleTypes.ADMIN, user.isActive(), user.getCreatedAt());

        } else if ("AGENT".equals(role)) {
            AgentEntity user = agentDao.findById(callerId)
                    .orElseThrow(() -> new DataValidationException("Agent not found"));
            return new UserProfileDto(user.getId(), user.getName(), user.getUserName(), user.getEmail(), RoleTypes.AGENT, user.isActive(), user.getCreatedAt());
        }

        throw new DataValidationException("Unauthorized role");
    }

    public List<AdminResponseDto> getAllAdminsForExport(Boolean active, String name, Date startDate, Date endDate) {

        List<AdminEntity> admins = adminDao.findAll();

        return admins.stream()
                .filter(admin -> active == null || admin.isActive() == active)
                .filter(admin -> name == null || admin.getName().toLowerCase().contains(name.toLowerCase()))
                .filter(admin -> {
                    if (startDate == null && endDate == null) return true;
                    Date created = admin.getCreatedAt();
                    return (startDate == null || !created.before(startDate)) &&
                            (endDate == null || !created.after(endDate));
                })
                .map(admin -> new AdminResponseDto(
                        admin.getId(),
                        admin.getName(),
                        admin.getUserName(),
                        admin.getEmail(),
                        admin.isActive(),
                        RoleTypes.ADMIN,
                        admin.getCreatedAt(),
                        walletService.getUsableBalanceForUser(admin.getId(), RoleTypes.ADMIN),
                        agentDao.countByAdminId(admin.getId())
                ))
                .toList();
    }

    public List<AgentResponseDto> getAllAgentsForExport(Boolean active, String name, Date startDate, Date endDate) {
        String role = UserContext.getRole();
        Long callerId = userContext.getUserId();

        List<AgentEntity> agents;

        if ("SUPER_ADMIN".equals(role)) {
            agents = agentDao.findAll();
        } else if ("ADMIN".equals(role)) {
            agents = agentDao.findByAdminId(callerId);
        } else {
            throw new DataValidationException("Unauthorized role to export agents.");
        }

        return agents.stream()
                .filter(agent -> active == null || agent.isActive() == active)
                .filter(agent -> name == null || agent.getName().toLowerCase().contains(name.toLowerCase()))
                .filter(agent -> {
                    if (startDate == null && endDate == null) return true;
                    Date created = agent.getCreatedAt();
                    return (startDate == null || !created.before(startDate)) &&
                            (endDate == null || !created.after(endDate));
                })
                .map(agent -> {
                    int playerCount = playerDao.countByAgentId(agent.getId());
                    String adminName = adminDao.findFirstById(agent.getAdminId()).getName();
                    return new AgentResponseDto(
                            agent.getId(),
                            agent.getName(),
                            agent.getUserName(),
                            agent.getEmail(),
                            agent.isActive(),
                            RoleTypes.AGENT,
                            agent.getCreatedAt(),
                            agent.getAdminId(),
                            adminName,
                            walletService.getUsableBalanceForUser(agent.getId(), RoleTypes.AGENT),
                            playerCount
                    );
                })
                .toList();
    }

}