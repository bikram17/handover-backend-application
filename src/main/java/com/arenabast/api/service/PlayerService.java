package com.arenabast.api.service;

import com.arenabast.api.auth.jwt.JwtUtil;
import com.arenabast.api.auth.jwt.UserContext;
import com.arenabast.api.dao.*;
import com.arenabast.api.dto.*;
import com.arenabast.api.entity.*;
import com.arenabast.api.enums.RoleTypes;
import com.arenabast.api.enums.TransactionApprovalStatus;
import com.arenabast.api.enums.TransactionType;
import com.arenabast.api.exception.DataValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlayerService {

    private final PlayerDao playerDao;
    private final WalletDao walletDao;
    private final AgentDao agentDao;
    private final WalletTransactionRequestDao walletTransactionRequestDao;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final TransactionLogDao transactionLogDao;
    private final UserContext userContext;
    private final AdminDao adminDao;

    public PlayerEntity createPlayer(CreatePlayerRequest request) {
        try {
            AgentEntity agent = validateAgent(request.getAgentId(), request.getAdminId());
            validatePlayer(request);
            PlayerEntity player = new PlayerEntity();
            player.setName(request.getName());
            player.setUserName(request.getUserName());
            player.setEmail(request.getEmail());
            player.setPasswordHash(passwordEncoder.encode(request.getPassword()));
            player.setAgentId(agent.getId());
            player.setPhone(request.getPhone());
            player.setPlayerId(String.format("%08d", (int) (Math.random() * 100_000_000)));
            if (request.getDateOfBirth() != null) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    Date dob = sdf.parse(request.getDateOfBirth());
                    player.setDateOfBirth(dob);
                } catch (Exception e) {
                    throw new DataValidationException("Invalid date of birth yyyy-MM-dd");
                }
            }
            player.setAddress(request.getAddress());
            player.setCreatedBy(agent.getEmail());
            player.setActive(true);

            PlayerEntity savedPlayer = playerDao.save(player);

            if (!walletDao.existsByPlayerId(savedPlayer.getId())) {
                WalletEntity wallet = new WalletEntity();
                wallet.setPlayerId(savedPlayer.getId());
                wallet.setCashBalance(0.0);
                wallet.setVirtualBalance(0.0);
                wallet.setTotalAddedByAgent(0.0);
                wallet.setTotalWon(0.0);
                wallet.setTotalLost(0.0);

                walletDao.save(wallet);
            }

            return savedPlayer;
        } catch (Exception e) {
            e.printStackTrace();
            throw new DataValidationException(e.getMessage());
        }
    }

    private void validatePlayer(CreatePlayerRequest request) {
        if (playerDao.existsByUserName(request.getUserName())) {
            throw new DataValidationException("Email already in use");
        }
    }

    private AgentEntity validateAgent(Long agentId, Long adminId) {
        RoleTypes role = RoleTypes.valueOf(UserContext.getRole());

        if (agentId == null) {
            if (role == RoleTypes.AGENT) {
                String email = UserContext.getEmail();
                AgentEntity agent = agentDao.findFirstByEmail(email)
                        .orElseThrow(() -> new DataValidationException("Agent not found for email: " + email));
                agentId = agent.getId();
            } else {
                throw new DataValidationException("Agent ID is required for this role");
            }
        }

        AgentEntity agent = agentDao.findById(agentId)
                .orElseThrow(() -> new DataValidationException("Invalid Agent ID"));

        if (!agent.isActive()) {
            throw new IllegalStateException("Agent is not active");
        }

        // Super admin-specific check: ensure agent is under admin
        if (role == RoleTypes.SUPER_ADMIN) {
            if (adminId == null) {
                throw new DataValidationException("Admin ID must be provided by Super Admin");
            }
            if (!agent.getAdminId().equals(adminId)) {
                throw new DataValidationException("Agent does not belong to the specified Admin");
            }
        }

        return agent;
    }

    public WalletTransactionRequestEntity requestWalletAction(Long playerId, TransactionType type, Double amount, String note) {
        PlayerEntity player = playerDao.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Player ID"));

        WalletEntity wallet = walletDao.findByPlayerId(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found"));

        WalletTransactionRequestEntity request = new WalletTransactionRequestEntity();
        request.setPlayerId(playerId);
        request.setAgentId(player.getAgentId());
        request.setTransactionType(type);
        request.setRequestedAmount(amount);
        request.setStatus(TransactionApprovalStatus.PENDING);
        request.setNote(note);

        if (type == TransactionType.WITHDRAW) {
            if (wallet.getCashBalance() < amount) {
                throw new IllegalStateException("Insufficient cash balance");
            }

            request.setLockedCashAmount(amount);
            // We don’t deduct cash yet — only on approval
        }

        return walletTransactionRequestDao.save(request);
    }

    public void confirmWithdrawalReceived(Long requestId, Long playerId) {
        WalletTransactionRequestEntity request = walletTransactionRequestDao.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));

        if (!request.getPlayerId().equals(playerId)) {
            throw new IllegalStateException("This withdrawal request does not belong to the player");
        }

        if (request.getTransactionType() != TransactionType.WITHDRAW ||
                request.getStatus() != TransactionApprovalStatus.APPROVED_PENDING_CLAIM) {
            throw new IllegalStateException("Cannot confirm this request");
        }

        WalletEntity wallet = walletDao.findByPlayerId(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found"));

        wallet.setCashBalance(wallet.getCashBalance() - request.getLockedCashAmount());
        walletDao.save(wallet);

        request.setStatus(TransactionApprovalStatus.COMPLETED);
        request.setResolvedAt(new Date());
        walletTransactionRequestDao.save(request);
    }

    public List<PlayerDto> getFilteredPlayers(Long agentIdParam) {
        String role = UserContext.getRole();
        Long callerId = userContext.getUserId();

        List<PlayerEntity> players;

        if ("SUPER_ADMIN".equals(role)) {
            // If agentId is supplied by super admin
            if (agentIdParam != null) {
                players = playerDao.findByAgentId(agentIdParam);
            } else {
                players = playerDao.findAll(); // All players
            }
        } else if ("ADMIN".equals(role)) {
            // Get all agents under this admin
            List<Long> agentIds = agentDao.findByAdminId(callerId).stream()
                    .map(AgentEntity::getId)
                    .toList();
            players = playerDao.findByAgentIdIn(agentIds);
        } else if ("AGENT".equals(role)) {
            // Agent can only see their players
            players = playerDao.findByAgentId(callerId);
        } else {
            throw new IllegalArgumentException("Unauthorized role");
        }

        return players.stream()
                .map(this::mapToDto)
                .toList();
    }

    public PlayerProfileDto getPlayerProfile(Long playerId) {
        PlayerEntity player = playerDao.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found"));

        WalletEntity wallet = walletDao.findByPlayerId(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found"));

        List<TransactionLogEntity> logs = transactionLogDao
                .findTop10ByCustomerIdOrderByCreatedAtDesc(playerId);

        PlayerProfileDto dto = new PlayerProfileDto();
        dto.setPlayer(mapToDto(player));
        dto.setWallet(mapWallet(wallet));
        dto.setRecentTransactions(logs.stream().map(this::mapTransaction).toList());
        return dto;
    }

    private PlayerDto mapToDto(PlayerEntity p) {
        PlayerDto dto = new PlayerDto();
        dto.setId(p.getId());
        dto.setName(p.getName());
        dto.setPlayerId(p.getPlayerId());
        dto.setEmail(p.getEmail());
        dto.setPhone(p.getPhone());
        dto.setUserName(p.getUserName());
        dto.setAddress(p.getAddress());
        dto.setAgentId(p.getAgentId());
        dto.setActive(p.isActive());
        dto.setDateOfBirth(p.getDateOfBirth());
        dto.setOnboardedDate(p.getCreatedAt()); // from BaseFieldsEntity

        try {
            AgentEntity agent = agentDao.findById(p.getAgentId())
                    .orElseThrow(() -> new RuntimeException("Agent not found"));

            dto.setAgentName(agent.getName());
            dto.setAdminId(agent.getAdminId());

            AdminEntity admin = adminDao.findById(agent.getAdminId())
                    .orElseThrow(() -> new RuntimeException("Admin not found"));

            dto.setAdminName(admin.getName());
        } catch (Exception e) {
            // In case agent/admin is deleted but player exists
            dto.setAgentName("Unknown");
            dto.setAdminName("Unknown");
        }

        // TODO: when login tracking exists
        dto.setPlayerLastLoginDate(null);

        return dto;
    }

    private PlayerWalletDto mapWallet(WalletEntity w) {
        PlayerWalletDto dto = new PlayerWalletDto();
        dto.setCashBalance(w.getCashBalance());
        dto.setVirtualBalance(w.getVirtualBalance());
        dto.setTotalAddedByAgent(w.getTotalAddedByAgent());
        dto.setTotalWon(w.getTotalWon());
        dto.setTotalLost(w.getTotalLost());
        return dto;
    }

    private TransactionLogDto mapTransaction(TransactionLogEntity e) {
        TransactionLogDto dto = new TransactionLogDto();
        dto.setTransactionId(e.getTransactionId());
        dto.setCustomerId(e.getCustomerId());
        dto.setAgentId(e.getAgentId());
        dto.setAdminId(e.getAdminId());
        dto.setIsWin(e.getIsWin());
        dto.setBetAmount(e.getBetAmount());
        dto.setWinAmount(e.getWinAmount());
        dto.setAgentCommission(e.getAgentCommission());
        dto.setAdminCommission(e.getAdminCommission());
        dto.setSuperAdminCommission(e.getSuperAdminCommission());
        dto.setExplanation(e.getExplanation());
        dto.setAgentSettled(e.getIsAgentSettled());
        dto.setAdminSettled(e.getIsAdminSettled());
        dto.setSuperAdminSettled(e.getIsSuperAdminSettled());
        return dto;
    }

    public PlayerDto updatePlayer(Long playerId, UpdatePlayerRequest request) {
        PlayerEntity player = playerDao.findById(playerId)
                .orElseThrow(() -> new DataValidationException("Player not found"));

        if (StringUtils.hasText(request.getName())) {
            player.setName(request.getName());
        }

        if (StringUtils.hasText(request.getPhone())) {
            player.setPhone(request.getPhone());
        }

        if (StringUtils.hasText(request.getAddress())) {
            player.setAddress(request.getAddress());
        }

        if (StringUtils.hasText(request.getPassword())) {
            player.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        if (StringUtils.hasText(request.getDateOfBirth())) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date dob = sdf.parse(request.getDateOfBirth());
                player.setDateOfBirth(dob);
            } catch (Exception e) {
                throw new DataValidationException("Invalid date format (expected yyyy-MM-dd)");
            }
        }

        if (request.getActive() != null) {
            player.setActive(request.getActive());
        }

        player.setUpdatedBy(UserContext.getEmail());
        playerDao.save(player);

        return mapToDto(player);
    }

    public void reassignPlayerAgentAndAdminBySuperAdmin(ChangePlayerAssignmentRequest req) {
        PlayerEntity player = playerDao.findById(req.getPlayerId())
                .orElseThrow(() -> new DataValidationException("Player not found"));

        AgentEntity agent = agentDao.findById(req.getNewAgentId())
                .orElseThrow(() -> new DataValidationException("Agent not found"));

        if (!agent.isActive()) {
            throw new DataValidationException("Agent is not active");
        }

        // If admin ID is supplied, update the agent's parent too
        if (req.getNewAdminId() != null) {
            AdminEntity admin = adminDao.findById(req.getNewAdminId())
                    .orElseThrow(() -> new DataValidationException("Admin not found"));
            agent.setAdminId(admin.getId());
            agentDao.save(agent);
        }

        player.setAgentId(agent.getId());
        player.setUpdatedBy(UserContext.getEmail());
        playerDao.save(player);
    }

    public PaginatedResponse<PlayerDto> getFilteredPlayers(Long agentIdParam, Boolean active, int page, int size) {
        String role = UserContext.getRole();
        Long callerId = userContext.getUserId();

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<PlayerEntity> playerPage;

        if ("SUPER_ADMIN".equals(role)) {
            if (agentIdParam != null) {
                if (active != null) {
                    playerPage = playerDao.findByAgentIdAndActive(agentIdParam, pageable, active);
                } else {
                    playerPage = playerDao.findByAgentId(agentIdParam, pageable);
                }
            } else {
                if (active != null) {
                    playerPage = playerDao.findByActive(active, pageable);
                } else {
                    playerPage = playerDao.findAll(pageable);
                }
            }
        } else if ("ADMIN".equals(role)) {
            List<Long> agentIds = agentDao.findByAdminId(callerId)
                    .stream()
                    .map(AgentEntity::getId)
                    .toList();
            if (active != null) {
                playerPage = playerDao.findByAgentIdInAndActive(agentIds, active, pageable);
            } else {
                playerPage = playerDao.findByAgentIdIn(agentIds, pageable);
            }
        } else if ("AGENT".equals(role)) {
            if (active != null) {
                playerPage = playerDao.findByAgentIdAndActive(callerId, pageable, active);
            } else {
                playerPage = playerDao.findByAgentId(callerId, pageable);
            }
        } else {
            throw new IllegalArgumentException("Unauthorized role");
        }

        List<PlayerDto> playerDtos = playerPage.getContent()
                .stream()
                .map(this::mapToDto)
                .toList();

        long activeCount = playerDao.countByActive(true);
        long suspendedCount = playerDao.countByActive(false);

        return new PaginatedResponse<>(
                playerDtos,
                playerPage.getTotalElements(),
                activeCount,
                suspendedCount,
                page,
                size
        );
    }

    public PlayerDto updatePlayerStatus(Long playerId, boolean active) {

        PlayerEntity player = playerDao.findById(playerId)
                .orElseThrow(() -> new DataValidationException("Player not found"));

        player.setActive(active);

        player.setUpdatedBy(UserContext.getEmail());
        playerDao.save(player);

        return mapToDto(player);
    }

    public WalletTransactionRequestEntity topUpWallet(Long playerId, TransactionType type, Double amount, String note) {

        if (type == TransactionType.WITHDRAW) {
            throw new DataValidationException("Withdrawal not supported");
        }

        PlayerEntity player = playerDao.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Player ID"));

        WalletEntity wallet = walletDao.findByPlayerId(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found"));

        WalletTransactionRequestEntity request = new WalletTransactionRequestEntity();
        request.setPlayerId(playerId);
        request.setAgentId(player.getAgentId());
        request.setTransactionType(type);
        request.setRequestedAmount(amount);
        request.setStatus(TransactionApprovalStatus.PENDING);
        request.setNote(note);
        request.setLockedCashAmount(amount);

        wallet.setCashBalance(wallet.getCashBalance() + amount);
        walletDao.save(wallet);
        return walletTransactionRequestDao.save(request);
    }
}