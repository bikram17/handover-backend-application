package com.arenabast.api.service;

import com.arenabast.api.auth.jwt.UserContext;
import com.arenabast.api.dao.*;
import com.arenabast.api.dto.*;
import com.arenabast.api.entity.*;
import com.arenabast.api.enums.RoleTypes;
import com.arenabast.api.enums.TransactionApprovalStatus;
import com.arenabast.api.enums.TransactionType;
import com.arenabast.api.exception.DataValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Stream;

@Service
@Slf4j
public class WalletService {

    private final WalletDao walletDao;
    private final PlayerDao playerDao;
    private final AgentDao agentDao;
    private final UserContext userContext;
    private final AdminDao adminDao;
    private final TransactionLogDao transactionLogDao;
    private final ManagementWalletDao managementWalletDao;
    private final OutstandingDao outstandingDao;
    private final SuperAdminDao superAdminDao;

    public WalletService(WalletDao walletDao, PlayerDao playerDao, AgentDao agentDao, UserContext userContext, AdminDao adminDao, TransactionLogDao transactionLogDao, ManagementWalletDao managementWalletDao, OutstandingDao outstandingDao, SuperAdminDao superAdminDao) {
        this.walletDao = walletDao;
        this.playerDao = playerDao;
        this.agentDao = agentDao;
        this.userContext = userContext;
        this.adminDao = adminDao;
        this.transactionLogDao = transactionLogDao;
        this.managementWalletDao = managementWalletDao;
        this.outstandingDao = outstandingDao;
        this.superAdminDao = superAdminDao;
    }

    public List<WalletViewDto> getWalletsBasedOnRole(String filterRole, Long agentIdParam) {
        String role = UserContext.getRole();
        Long callerId = userContext.getUserId();

        List<WalletViewDto> result = new ArrayList<>();

        if ("SUPER_ADMIN".equals(role)) {
            // Players
            List<WalletEntity> playerWallets = walletDao.findAll();
            result.addAll(playerWallets.stream().map(this::mapToDto).toList());

            // Agents
            result.addAll(managementWalletDao.findByRole(RoleTypes.AGENT).stream().map(this::mapToDto).toList());

            // Admins
            result.addAll(managementWalletDao.findByRole(RoleTypes.ADMIN).stream().map(this::mapToDto).toList());

            // Super Admin (only one)
            result.addAll(managementWalletDao.findByRole(RoleTypes.SUPER_ADMIN).stream().map(this::mapToDto).toList());

        } else if ("ADMIN".equals(role)) {
            // Agents under this admin
            List<Long> agentIds = agentDao.findByAdminId(callerId).stream()
                    .map(AgentEntity::getId).toList();
            result.addAll(managementWalletDao.findByUserIdInAndRole(agentIds, RoleTypes.AGENT).stream().map(this::mapToDto).toList());

            // Players under those agents
            List<Long> playerIds = playerDao.findByAgentIdIn(agentIds).stream()
                    .map(PlayerEntity::getId).toList();
            result.addAll(walletDao.findByPlayerIdIn(playerIds).stream().map(this::mapToDto).toList());

            // Admin’s own wallet
            managementWalletDao.findByUserIdAndRole(callerId, RoleTypes.ADMIN).ifPresent(
                    mw -> result.add(mapToDto(mw))
            );

        } else if ("AGENT".equals(role)) {
            // Players under this agent
            List<Long> playerIds = playerDao.findByAgentId(callerId).stream()
                    .map(PlayerEntity::getId).toList();
            result.addAll(walletDao.findByPlayerIdIn(playerIds).stream().map(this::mapToDto).toList());

            // Agent’s own wallet
            managementWalletDao.findByUserIdAndRole(callerId, RoleTypes.AGENT).ifPresent(
                    mw -> result.add(mapToDto(mw))
            );
        }

        return result;
    }

    private WalletViewDto mapToDto(WalletEntity wallet) {
        WalletViewDto dto = new WalletViewDto();
        dto.setUserId(wallet.getPlayerId()); // fallback
        dto.setCashBalance(wallet.getCashBalance());
        dto.setVirtualBalance(wallet.getVirtualBalance());
        dto.setTotalAddedByAgent(wallet.getTotalAddedByAgent());
        dto.setTotalWon(wallet.getTotalWon());
        dto.setTotalLost(wallet.getTotalLost());

        // From transaction logs (simulate winnings/losses)
//        Double totalCommission = transactionLogDao.getCommissionForUser(wallet.getPlayerId());
        Double totalCommission = 0.0;
        dto.setNetCommissionEarned(totalCommission != null ? totalCommission : 0.0);

        // enrich name & role
        if (playerDao.existsById(wallet.getPlayerId())) {
            PlayerEntity p = playerDao.findById(wallet.getPlayerId()).get();
            dto.setUserName(p.getName());
            dto.setRole("PLAYER");
            dto.setUserId(p.getId());
        } else if (agentDao.existsById(wallet.getPlayerId())) {
            AgentEntity a = agentDao.findById(wallet.getPlayerId()).get();
            dto.setUserName(a.getName());
            dto.setRole("AGENT");
            dto.setUserId(a.getId());
        } else if (adminDao.existsById(wallet.getPlayerId())) {
            AdminEntity a = adminDao.findById(wallet.getPlayerId()).get();
            dto.setUserName(a.getName());
            dto.setRole("ADMIN");
            dto.setUserId(a.getId());
        }

        return dto;
    }

    private WalletViewDto mapToDto(ManagementWalletEntity mw) {
        WalletViewDto dto = new WalletViewDto();
        dto.setUserId(mw.getUserId());
        dto.setRole(mw.getRole().name());
        dto.setCashBalance(mw.getBalance());
        dto.setVirtualBalance(0.0); // not applicable
        dto.setTotalAddedByAgent(0.0); // optional
        dto.setTotalWon(0.0);
        dto.setTotalLost(0.0);
        dto.setNetCommissionEarned(mw.getTotalReceived());

        // Set name
        switch (mw.getRole()) {
            case AGENT -> dto.setUserName(agentDao.findById(mw.getUserId()).map(AgentEntity::getName).orElse("N/A"));
            case ADMIN -> dto.setUserName(adminDao.findById(mw.getUserId()).map(AdminEntity::getName).orElse("N/A"));
            case SUPER_ADMIN -> dto.setUserName("Super Admin");
        }

        return dto;
    }

    public void selfTopUpWallet(AddBalanceRequest request) {
        String role = UserContext.getRole(); // must be SUPER_ADMIN
        Long callerId = userContext.getUserId();
        Long targetId = request.getUserId() != null ? request.getUserId() : callerId;

        // 🛡 Role check
        if (!"SUPER_ADMIN".equals(role)) {
            throw new DataValidationException("Only Super Admin is authorized to top up their own wallet");
        }

        // 🛡 Self-only restriction
        if (request.getUserId() != null && !callerId.equals(targetId)) {
            throw new DataValidationException("You can only top up your own wallet");
        }

        // 🛡 Amount validation
        if (request.getAmount() == null || request.getAmount() <= 0) {
            throw new DataValidationException("Amount must be greater than 0");
        }

        // 💰 Fetch wallet
        ManagementWalletEntity wallet = managementWalletDao
                .findByUserIdAndRole(callerId, RoleTypes.SUPER_ADMIN)
                .orElseThrow(() -> new DataValidationException("Super Admin wallet not found"));

        // 📊 Capture balances before update
        double prevBalance = wallet.getBalance();

        // 💵 Update balance once
        wallet.setBalance(prevBalance + request.getAmount());
        wallet.setTotalReceived(wallet.getTotalReceived() + request.getAmount());
        managementWalletDao.save(wallet);

        double currentBalance = wallet.getBalance();

        // 📝 Log transaction
        TransactionLogEntity log = new TransactionLogEntity();
        log.setTransactionId(UUID.randomUUID().toString());
        log.setTransactionType(TransactionType.DEPOSIT);

        log.setCustomerId(null);
        log.setSuperAdminId(callerId);
        log.setBetAmount(request.getAmount());
        log.setIsWin(null);
        log.setWinAmount(null);

        log.setAgentCommission(0.0);
        log.setAdminCommission(0.0);
        log.setSuperAdminCommission(0.0);

        log.setIsWinPayoutClaimed(false);
        log.setIsAgentSettled(true);
        log.setIsAdminSettled(true);
        log.setIsSuperAdminSettled(true);

        Date now = new Date();
        log.setAgentSettledAt(now);
        log.setAdminSettledAt(now);
        log.setSuperAdminSettledAt(now);

        String note = request.getNote() != null ? request.getNote() : "Top-up";
        log.setExplanation("SELF-TOPUP: $" + request.getAmount() + " added to Super Admin wallet. Note: " + note);

        log.setPreviousBalance(prevBalance);
        log.setCurrentBalance(currentBalance);


        transactionLogDao.save(log);
    }

    public void addToPlayerWallet(AddBalanceRequest request) {
        String role = UserContext.getRole();
        Long callerId = userContext.getUserId();

        PlayerEntity player = playerDao.findById(request.getUserId())
                .orElseThrow(() -> new DataValidationException("Player not found"));

        if ("SUPER_ADMIN".equals(role)) {
            // all good
        } else if ("ADMIN".equals(role)) {
            List<Long> agentIds = agentDao.findByAdminId(callerId).stream()
                    .map(AgentEntity::getId).toList();
            if (!agentIds.contains(player.getAgentId())) {
                throw new DataValidationException("Player not under your agents");
            }
        } else if ("AGENT".equals(role)) {
            if (!player.getAgentId().equals(callerId)) {
                throw new DataValidationException("Player not under your agency");
            }
        } else {
            throw new DataValidationException("Unauthorized role");
        }

        WalletEntity wallet = walletDao.findByPlayerId(request.getUserId())
                .orElseThrow(() -> new DataValidationException("Wallet not found"));

        wallet.setCashBalance(wallet.getCashBalance() + request.getAmount());
        wallet.setTotalAddedByAgent(wallet.getTotalAddedByAgent() + request.getAmount());
        walletDao.save(wallet);
    }

    public void addCashToPlayerByAgent(AddPlayerBalanceRequest req) {
        Long agentId = userContext.getUserId();

        PlayerEntity player = playerDao.findById(req.getPlayerId())
                .orElseThrow(() -> new DataValidationException("Invalid Player"));

        if (!player.getAgentId().equals(agentId)) {
            throw new DataValidationException("This player does not belong to you");
        }

        // Get wallets
        WalletEntity playerWallet = walletDao.findByPlayerId(req.getPlayerId())
                .orElseThrow(() -> new DataValidationException("Wallet not found"));
        ManagementWalletEntity agentWallet = managementWalletDao
                .findByUserIdAndRole(agentId, RoleTypes.AGENT)
                .orElseThrow(() -> new DataValidationException("Agent wallet not found"));

        // Balance check for agent
        if (agentWallet.getBalance() < req.getAmount()) {
            throw new DataValidationException("Agent doesn't have sufficient balance");
        }

        // Capture balances before update
        double prevPlayerBalance = playerWallet.getCashBalance() + playerWallet.getVirtualBalance();
        double prevAgentBalance = agentWallet.getBalance();

        // Perform transfer
        agentWallet.setBalance(prevAgentBalance - req.getAmount());
        agentWallet.setTotalSettled(agentWallet.getTotalSettled() + req.getAmount());

        playerWallet.setCashBalance(playerWallet.getCashBalance() + req.getAmount());
        playerWallet.setTotalAddedByAgent(playerWallet.getTotalAddedByAgent() + req.getAmount());

        managementWalletDao.save(agentWallet);
        walletDao.save(playerWallet);

        double currentPlayerBalance = playerWallet.getCashBalance() + playerWallet.getVirtualBalance();
        double currentAgentBalance = agentWallet.getBalance();

        AgentEntity agent = agentDao.findById(agentId).orElseThrow();
        AdminEntity admin = adminDao.findById(agent.getAdminId()).orElse(null);

        Date now = new Date();

        // 📜 CREDIT log for player
        TransactionLogEntity creditLog = new TransactionLogEntity();
        creditLog.setCustomerId(req.getPlayerId());
        creditLog.setAgentId(agentId);
        creditLog.setAdminId(admin != null ? admin.getId() : null);
        creditLog.setSuperAdminId(1L);
        creditLog.setTransactionType(TransactionType.DEPOSIT);
        creditLog.setBetAmount(req.getAmount());
        creditLog.setIsWin(null);
        creditLog.setWinAmount(null);
        creditLog.setAgentCommission(0.0);
        creditLog.setAdminCommission(0.0);
        creditLog.setSuperAdminCommission(0.0);
        creditLog.setIsWinPayoutClaimed(false);
        creditLog.setIsAgentSettled(true);
        creditLog.setIsAdminSettled(true);
        creditLog.setIsSuperAdminSettled(false);
        creditLog.setAgentSettledAt(now);
        creditLog.setAdminSettledAt(now);
        creditLog.setTransactionId(UUID.randomUUID().toString());
        creditLog.setExplanation("DEPOSIT (CREDIT): $" + req.getAmount() + " added to Player [" + player.getName() + "] by Agent [" + agent.getName() + "]");
        creditLog.setPreviousBalance(prevPlayerBalance);
        creditLog.setCurrentBalance(currentPlayerBalance);

        transactionLogDao.save(creditLog);

        // 📜 DEBIT log for agent
        TransactionLogEntity debitLog = new TransactionLogEntity();
        debitLog.setCustomerId(null);
        debitLog.setAgentId(agentId);
        debitLog.setAdminId(admin != null ? admin.getId() : null);
        debitLog.setSuperAdminId(1L);
        debitLog.setTransactionType(TransactionType.TRANSFER);
        debitLog.setBetAmount(req.getAmount());
        debitLog.setIsWin(null);
        debitLog.setWinAmount(null);
        debitLog.setAgentCommission(0.0);
        debitLog.setAdminCommission(0.0);
        debitLog.setSuperAdminCommission(0.0);
        debitLog.setIsWinPayoutClaimed(false);
        debitLog.setIsAgentSettled(true);
        debitLog.setIsAdminSettled(true);
        debitLog.setIsSuperAdminSettled(false);
        debitLog.setAgentSettledAt(now);
        debitLog.setAdminSettledAt(now);
        debitLog.setTransactionId(UUID.randomUUID().toString());
        debitLog.setExplanation("TRANSFER (DEBIT): $" + req.getAmount() + " sent to Player [" + player.getName() + "]");
        debitLog.setPreviousBalance(prevAgentBalance);
        debitLog.setCurrentBalance(currentAgentBalance);

        transactionLogDao.save(debitLog);
    }

    public void addCashToManagementWallet(AddBalanceRequest req) {
        String callerRole = UserContext.getRole(); // "SUPER_ADMIN", "ADMIN"
        Long callerId = userContext.getUserId();
        Long receiverId = req.getUserId();
        RoleTypes receiverRole = req.getUserRole(); // AGENT or ADMIN only

        // 🛡️ Super Admin check (cannot top-up self)
        if ("SUPER_ADMIN".equals(callerRole) && receiverRole == RoleTypes.SUPER_ADMIN) {
            throw new DataValidationException("You cannot add cash to Super Admin wallet");
        }

        // 🛡️ Safety: Only allow top-up to AGENT or ADMIN
        if (receiverRole != RoleTypes.AGENT && receiverRole != RoleTypes.ADMIN) {
            throw new DataValidationException("You can only add cash to agents or admins");
        }

        // 🛡️ Admin check — only their agents
        if ("ADMIN".equals(callerRole)) {
            if (receiverRole != RoleTypes.AGENT) {
                throw new DataValidationException("Admins can only top up their agents");
            }

            AgentEntity agent = agentDao.findById(receiverId)
                    .orElseThrow(() -> new DataValidationException("Agent not found"));

            if (!agent.getAdminId().equals(callerId)) {
                throw new DataValidationException("Agent not under your control");
            }
        }

        // 🛠️ Get wallets
        ManagementWalletEntity receiverWallet = managementWalletDao
                .findByUserIdAndRole(receiverId, receiverRole)
                .orElseThrow(() -> new DataValidationException("Receiver wallet not found"));

        ManagementWalletEntity callerWallet = managementWalletDao
                .findByUserIdAndRole(callerId, RoleTypes.valueOf(callerRole))
                .orElseThrow(() -> new DataValidationException("Caller wallet not found"));

        // 💰 Balance check for caller (optional, only if it’s a real deduction)
        if (callerWallet.getBalance() < req.getAmount()) {
            throw new DataValidationException("Insufficient balance in caller wallet");
        }

        // Save previous balances
        double prevReceiverBalance = receiverWallet.getBalance();
        double prevCallerBalance = callerWallet.getBalance();

        // Transfer funds
        callerWallet.setBalance(prevCallerBalance - req.getAmount());
        callerWallet.setTotalSettled(callerWallet.getTotalSettled() + req.getAmount());

        receiverWallet.setBalance(prevReceiverBalance + req.getAmount());
        receiverWallet.setTotalReceived(receiverWallet.getTotalReceived() + req.getAmount());

        managementWalletDao.save(callerWallet);
        managementWalletDao.save(receiverWallet);

        Date now = new Date();

        // 📜 CREDIT log for receiver
        TransactionLogEntity creditLog = new TransactionLogEntity();
        creditLog.setTransactionId(UUID.randomUUID().toString());
        creditLog.setTransactionType(TransactionType.DEPOSIT);
        creditLog.setBetAmount(req.getAmount());
        creditLog.setIsWin(null);
        creditLog.setWinAmount(null);
        creditLog.setAgentCommission(0.0);
        creditLog.setAdminCommission(0.0);
        creditLog.setSuperAdminCommission(0.0);
        creditLog.setIsWinPayoutClaimed(false);
        creditLog.setIsAgentSettled(true);
        creditLog.setIsAdminSettled(true);
        creditLog.setIsSuperAdminSettled(true);
        creditLog.setAgentSettledAt(now);
        creditLog.setAdminSettledAt(now);
        creditLog.setSuperAdminSettledAt(now);

        if (receiverRole == RoleTypes.AGENT) creditLog.setAgentId(receiverId);
        if (receiverRole == RoleTypes.ADMIN) creditLog.setAdminId(receiverId);
        if ("SUPER_ADMIN".equals(callerRole)) creditLog.setSuperAdminId(callerId);
        if ("ADMIN".equals(callerRole)) creditLog.setAdminId(callerId);

        creditLog.setExplanation("DEPOSIT (CREDIT): $" + req.getAmount() + " added by " +
                callerRole + " [ID: " + callerId + "]");
        creditLog.setPreviousBalance(prevReceiverBalance);
        creditLog.setCurrentBalance(receiverWallet.getBalance());

        transactionLogDao.save(creditLog);

        // 📜 DEBIT log for caller
        TransactionLogEntity debitLog = new TransactionLogEntity();
        debitLog.setTransactionId(UUID.randomUUID().toString());
        debitLog.setTransactionType(TransactionType.TRANSFER);
        debitLog.setBetAmount(req.getAmount());
        debitLog.setIsWin(null);
        debitLog.setWinAmount(null);
        debitLog.setAgentCommission(0.0);
        debitLog.setAdminCommission(0.0);
        debitLog.setSuperAdminCommission(0.0);
        debitLog.setIsWinPayoutClaimed(false);
        debitLog.setIsAgentSettled(true);
        debitLog.setIsAdminSettled(true);
        debitLog.setIsSuperAdminSettled(true);
        debitLog.setAgentSettledAt(now);
        debitLog.setAdminSettledAt(now);
        debitLog.setSuperAdminSettledAt(now);

        if ("SUPER_ADMIN".equals(callerRole)) debitLog.setSuperAdminId(callerId);
        if ("ADMIN".equals(callerRole)) debitLog.setAdminId(callerId);

        debitLog.setExplanation("TRANSFER (DEBIT): $" + req.getAmount() + " sent to " +
                receiverRole + " [ID: " + receiverId + "]");
        debitLog.setPreviousBalance(prevCallerBalance);
        debitLog.setCurrentBalance(callerWallet.getBalance());

        transactionLogDao.save(debitLog);
    }

    public void initiateAgentToAdminSettlement(AgentToAdminSettlementRequest req) {
        Long agentId = userContext.getUserId();

        AgentEntity agent = agentDao.findById(agentId)
                .orElseThrow(() -> new DataValidationException("Agent not found"));
        Long adminId = agent.getAdminId();

        OutstandingEntity outstanding = new OutstandingEntity();
        outstanding.setFromUserId(agentId);
        outstanding.setToUserId(adminId);
        outstanding.setAmount(req.getAmount());
        outstanding.setStatus(TransactionApprovalStatus.PENDING);
        outstanding.setNote(req.getNote());
        outstanding.setResolvedAt(null);

        outstanding.setRoleType(RoleTypes.AGENT); // ✅ Important line

        outstandingDao.save(outstanding);
    }

    public void approveAgentToAdminSettlement(Long requestId) {
        Long adminId = userContext.getUserId();

        OutstandingEntity request = outstandingDao.findById(requestId)
                .orElseThrow(() -> new DataValidationException("Request not found"));

        if (!request.getToUserId().equals(adminId)) {
            throw new DataValidationException("You are not authorized to approve this request");
        }

        if (!TransactionApprovalStatus.PENDING.equals(request.getStatus())) {
            throw new DataValidationException("Request already processed");
        }

        // Wallets
        ManagementWalletEntity agentWallet = managementWalletDao.findByUserIdAndRole(request.getFromUserId(), RoleTypes.AGENT)
                .orElseThrow(() -> new DataValidationException("Agent wallet not found"));

        ManagementWalletEntity adminWallet = managementWalletDao.findByUserIdAndRole(adminId, RoleTypes.ADMIN)
                .orElseThrow(() -> new DataValidationException("Admin wallet not found"));

        // Sufficient balance check
        if (agentWallet.getBalance() < request.getAmount()) {
            throw new DataValidationException("Agent doesn't have sufficient balance");
        }

        // Transfer
        double prevAgentBalance = agentWallet.getBalance();
        double prevAdminBalance = adminWallet.getBalance();

        agentWallet.setBalance(prevAgentBalance - request.getAmount());
        agentWallet.setTotalSettled(agentWallet.getTotalSettled() + request.getAmount());

        adminWallet.setBalance(prevAdminBalance + request.getAmount());
        adminWallet.setTotalReceived(adminWallet.getTotalReceived() + request.getAmount());

        managementWalletDao.save(agentWallet);
        managementWalletDao.save(adminWallet);

        // Mark request as resolved
        request.setStatus(TransactionApprovalStatus.APPROVED);
        request.setResolvedAt(new Date());
        outstandingDao.save(request);

        Date now = new Date();

        // 🔹 1. Log Debit (Agent)
        TransactionLogEntity debitLog = new TransactionLogEntity();
        debitLog.setAgentId(agentWallet.getUserId());
        debitLog.setAdminId(adminId);
        debitLog.setSuperAdminId(1L); // or null if not relevant
        debitLog.setTransactionType(TransactionType.TRANSFER);
        debitLog.setBetAmount(request.getAmount());
        debitLog.setIsWin(null);
        debitLog.setWinAmount(null);
        debitLog.setAgentCommission(0.0);
        debitLog.setAdminCommission(0.0);
        debitLog.setSuperAdminCommission(0.0);
        debitLog.setIsWinPayoutClaimed(false);
        debitLog.setIsAgentSettled(true);
        debitLog.setIsAdminSettled(true);
        debitLog.setIsSuperAdminSettled(false);
        debitLog.setAgentSettledAt(now);
        debitLog.setAdminSettledAt(now);
        debitLog.setTransactionId(UUID.randomUUID().toString());
        debitLog.setExplanation("TRANSFER (DEBIT): $" + request.getAmount() + " sent from Agent → Admin");
        debitLog.setPreviousBalance(prevAgentBalance);
        debitLog.setCurrentBalance(agentWallet.getBalance());

        transactionLogDao.save(debitLog);

        // 🔹 2. Log Credit (Admin)
        TransactionLogEntity creditLog = new TransactionLogEntity();
        creditLog.setAgentId(agentWallet.getUserId());
        creditLog.setAdminId(adminId);
        creditLog.setSuperAdminId(1L);
        creditLog.setTransactionType(TransactionType.TRANSFER);
        creditLog.setBetAmount(request.getAmount());
        creditLog.setIsWin(null);
        creditLog.setWinAmount(null);
        creditLog.setAgentCommission(0.0);
        creditLog.setAdminCommission(0.0);
        creditLog.setSuperAdminCommission(0.0);
        creditLog.setIsWinPayoutClaimed(false);
        creditLog.setIsAgentSettled(true);
        creditLog.setIsAdminSettled(true);
        creditLog.setIsSuperAdminSettled(false);
        creditLog.setAgentSettledAt(now);
        creditLog.setAdminSettledAt(now);
        creditLog.setTransactionId(UUID.randomUUID().toString());
        creditLog.setExplanation("TRANSFER (CREDIT): $" + request.getAmount() + " received from Agent");
        creditLog.setPreviousBalance(prevAdminBalance);
        creditLog.setCurrentBalance(adminWallet.getBalance());

        transactionLogDao.save(creditLog);
    }

    public void initiateAdminToSuperAdminSettlement(AdminToSuperAdminSettlementRequest req) {
        Long adminId = userContext.getUserId();
        Long superAdminId = 1L;

        OutstandingEntity outstanding = new OutstandingEntity();
        outstanding.setFromUserId(adminId);
        outstanding.setToUserId(superAdminId);
        outstanding.setAmount(req.getAmount());
        outstanding.setStatus(TransactionApprovalStatus.PENDING);
        outstanding.setNote(req.getNote());
        outstanding.setResolvedAt(null);

        outstanding.setRoleType(RoleTypes.ADMIN); // ✅ Important line

        outstandingDao.save(outstanding);
    }

    public void approveAdminToSuperAdminSettlement(Long requestId) {
        Long superAdminId = userContext.getUserId();

        OutstandingEntity request = outstandingDao.findById(requestId)
                .orElseThrow(() -> new DataValidationException("Request not found"));

        if (!request.getToUserId().equals(superAdminId)) {
            throw new DataValidationException("You are not authorized to approve this request");
        }

        if (!TransactionApprovalStatus.PENDING.equals(request.getStatus())) {
            throw new DataValidationException("Request already processed");
        }

        // Wallets
        ManagementWalletEntity adminWallet = managementWalletDao
                .findByUserIdAndRole(request.getFromUserId(), RoleTypes.ADMIN)
                .orElseThrow(() -> new DataValidationException("Admin wallet not found"));

        ManagementWalletEntity superWallet = managementWalletDao
                .findByUserIdAndRole(superAdminId, RoleTypes.SUPER_ADMIN)
                .orElseThrow(() -> new DataValidationException("SuperAdmin wallet not found"));

        // Sufficient balance check
        if (adminWallet.getBalance() < request.getAmount()) {
            throw new DataValidationException("Admin doesn't have sufficient balance");
        }

        // Capture balances before update
        double adminPrevBalance = adminWallet.getBalance();
        double superPrevBalance = superWallet.getBalance();

        // Transfer
        adminWallet.setBalance(adminWallet.getBalance() - request.getAmount());
        adminWallet.setTotalSettled(adminWallet.getTotalSettled() + request.getAmount());

        superWallet.setBalance(superWallet.getBalance() + request.getAmount());
        superWallet.setTotalReceived(superWallet.getTotalReceived() + request.getAmount());

        managementWalletDao.save(adminWallet);
        managementWalletDao.save(superWallet);

        double adminCurrentBalance = adminWallet.getBalance();
        double superCurrentBalance = superWallet.getBalance();

        // Mark request resolved
        request.setStatus(TransactionApprovalStatus.APPROVED);
        request.setResolvedAt(new Date());
        outstandingDao.save(request);

        Date now = new Date();

        // Log: Admin perspective (debit)
        TransactionLogEntity adminLog = new TransactionLogEntity();
        adminLog.setAdminId(adminWallet.getUserId());
        adminLog.setSuperAdminId(superAdminId);
        adminLog.setTransactionType(TransactionType.TRANSFER);
        adminLog.setBetAmount(-request.getAmount()); // Negative for debit
        adminLog.setIsWin(null);
        adminLog.setAgentCommission(0.0);
        adminLog.setAdminCommission(0.0);
        adminLog.setSuperAdminCommission(0.0);
        adminLog.setIsWinPayoutClaimed(false);
        adminLog.setIsAgentSettled(true);
        adminLog.setIsAdminSettled(true);
        adminLog.setIsSuperAdminSettled(true);
        adminLog.setAdminSettledAt(now);
        adminLog.setSuperAdminSettledAt(now);
        adminLog.setTransactionId(UUID.randomUUID().toString());
        adminLog.setExplanation("TRANSFER OUT: $" + request.getAmount() + " sent to Super Admin");
        adminLog.setPreviousBalance(adminPrevBalance);
        adminLog.setCurrentBalance(adminCurrentBalance);
        transactionLogDao.save(adminLog);

        // Log: Super Admin perspective (credit)
        TransactionLogEntity superLog = new TransactionLogEntity();
        superLog.setAdminId(adminWallet.getUserId());
        superLog.setSuperAdminId(superAdminId);
        superLog.setTransactionType(TransactionType.TRANSFER);
        superLog.setBetAmount(request.getAmount()); // Positive for credit
        superLog.setIsWin(null);
        superLog.setAgentCommission(0.0);
        superLog.setAdminCommission(0.0);
        superLog.setSuperAdminCommission(0.0);
        superLog.setIsWinPayoutClaimed(false);
        superLog.setIsAgentSettled(true);
        superLog.setIsAdminSettled(true);
        superLog.setIsSuperAdminSettled(true);
        superLog.setAdminSettledAt(now);
        superLog.setSuperAdminSettledAt(now);
        superLog.setTransactionId(UUID.randomUUID().toString());
        superLog.setExplanation("TRANSFER IN: $" + request.getAmount() + " received from Admin");
        superLog.setPreviousBalance(superPrevBalance);
        superLog.setCurrentBalance(superCurrentBalance);
        transactionLogDao.save(superLog);
    }

    public List<SettlementRequestDto> getPendingSettlementRequestsForUser(Long userId) {
        List<OutstandingEntity> requests = outstandingDao.findByToUserIdAndStatus(userId, TransactionApprovalStatus.PENDING);
        return requests.stream().map(this::mapToDto).toList();
    }

    public List<SettlementRequestDto> getAllRequestsForUser(Long userId) {
        List<OutstandingEntity> requests = outstandingDao.findByToUserId(userId);
        return requests.stream().map(this::mapToDto).toList();
    }

    public SettlementRequestsSummaryDto getAllRequestsSummaryForUser(Long userId, TransactionApprovalStatus approvalStatus) {
        List<OutstandingEntity> requests = outstandingDao.findByToUserId(userId);

        long approvedCount = requests.stream()
                .filter(r -> r.getStatus() == TransactionApprovalStatus.APPROVED)
                .count();
        double approvedAmount = requests.stream()
                .filter(r -> r.getStatus() == TransactionApprovalStatus.APPROVED)
                .mapToDouble(OutstandingEntity::getAmount)
                .sum();

        long rejectedCount = requests.stream()
                .filter(r -> r.getStatus() == TransactionApprovalStatus.REJECTED)
                .count();
        double rejectedAmount = requests.stream()
                .filter(r -> r.getStatus() == TransactionApprovalStatus.REJECTED)
                .mapToDouble(OutstandingEntity::getAmount)
                .sum();

        long pendingCount = requests.stream()
                .filter(r -> r.getStatus() == TransactionApprovalStatus.PENDING)
                .count();
        double pendingAmount = requests.stream()
                .filter(r -> r.getStatus() == TransactionApprovalStatus.PENDING)
                .mapToDouble(OutstandingEntity::getAmount)
                .sum();

        // ✅ Filter requests list only if approvalStatus is provided
        Stream<OutstandingEntity> filteredRequests = requests.stream();
        if (approvalStatus != null) {
            filteredRequests = filteredRequests.filter(r -> r.getStatus() == approvalStatus);
        }

        List<SettlementRequestDto> dtos = filteredRequests
                .sorted(Comparator.comparing(OutstandingEntity::getCreatedAt).reversed()) // optional: sort latest first
                .map(this::mapToDto)
                .toList();

        return new SettlementRequestsSummaryDto(
                approvedCount, approvedAmount,
                rejectedCount, rejectedAmount,
                pendingCount, pendingAmount,
                dtos
        );
    }

    private SettlementRequestDto mapToDto(OutstandingEntity o) {
        SettlementRequestDto dto = new SettlementRequestDto();
        dto.setRequestId(o.getId());
        dto.setFromUserId(o.getFromUserId());
        dto.setToUserId(o.getToUserId());
        dto.setAmount(o.getAmount());
        dto.setNote(o.getNote());
        dto.setStatus(o.getStatus());
        dto.setCreatedAt(o.getCreatedAt());
        dto.setResolvedAt(o.getResolvedAt());

        // From User
        if (agentDao.existsById(o.getFromUserId())) {
            dto.setFromUserRole(RoleTypes.AGENT);
            dto.setFromUserName(agentDao.findById(o.getFromUserId()).map(AgentEntity::getName).orElse("Unknown"));
        } else if (adminDao.existsById(o.getFromUserId())) {
            dto.setFromUserRole(RoleTypes.ADMIN);
            dto.setFromUserName(adminDao.findById(o.getFromUserId()).map(AdminEntity::getName).orElse("Unknown"));
        }

        // To User
        if (adminDao.existsById(o.getToUserId())) {
            dto.setToUserRole(RoleTypes.ADMIN);
            dto.setToUserName(adminDao.findById(o.getToUserId()).map(AdminEntity::getName).orElse("Unknown"));
        } else if (superAdminDao.existsById(o.getToUserId())) {
            dto.setToUserRole(RoleTypes.SUPER_ADMIN);
            dto.setToUserName("Super Admin");
        }

        return dto;
    }

    public void processSettlement(boolean approvalStatus, Long requestId) {
        if (approvalStatus) {
            approveGenericSettlement(requestId);
        } else rejectSettlement(requestId);
    }

    public void approveGenericSettlement(Long requestId) {
        Long currentUserId = userContext.getUserId();
        OutstandingEntity request = outstandingDao.findById(requestId)
                .orElseThrow(() -> new DataValidationException("Request not found"));

        if (!request.getToUserId().equals(currentUserId)) {
            throw new DataValidationException("You are not allowed to approve this request");
        }

        if (!request.getStatus().equals(TransactionApprovalStatus.PENDING)) {
            throw new DataValidationException("Request already resolved");
        }

        RoleTypes toRole = getUserRole(request.getToUserId());
        RoleTypes fromRole = getUserRole(request.getFromUserId());

        ManagementWalletEntity senderWallet = managementWalletDao
                .findByUserIdAndRole(request.getFromUserId(), fromRole)
                .orElseThrow(() -> new DataValidationException("Sender wallet not found"));

        ManagementWalletEntity receiverWallet = managementWalletDao
                .findByUserIdAndRole(request.getToUserId(), toRole)
                .orElseThrow(() -> new DataValidationException("Receiver wallet not found"));

        if (senderWallet.getBalance() < request.getAmount()) {
            throw new DataValidationException("Insufficient balance in sender's wallet");
        }

        // Save previous balances for logs
        double prevSenderBalance = senderWallet.getBalance();
        double prevReceiverBalance = receiverWallet.getBalance();

        // Perform transfer
        senderWallet.setBalance(prevSenderBalance - request.getAmount());
        senderWallet.setTotalSettled(senderWallet.getTotalSettled() + request.getAmount());

        receiverWallet.setBalance(prevReceiverBalance + request.getAmount());
        receiverWallet.setTotalReceived(receiverWallet.getTotalReceived() + request.getAmount());

        managementWalletDao.save(senderWallet);
        managementWalletDao.save(receiverWallet);

        // Update request
        request.setStatus(TransactionApprovalStatus.APPROVED);
        request.setResolvedAt(new Date());
        outstandingDao.save(request);

        Date now = new Date();

        // 🔹 Sender DEBIT log
        TransactionLogEntity debitLog = new TransactionLogEntity();
        debitLog.setAgentId(fromRole == RoleTypes.AGENT ? senderWallet.getUserId() : null);
        debitLog.setAdminId(fromRole == RoleTypes.ADMIN ? senderWallet.getUserId() : null);
        debitLog.setSuperAdminId(fromRole == RoleTypes.SUPER_ADMIN ? senderWallet.getUserId() : null);
        debitLog.setTransactionType(TransactionType.TRANSFER);
        debitLog.setBetAmount(request.getAmount());
        debitLog.setIsWin(null);
        debitLog.setWinAmount(null);
        debitLog.setAgentCommission(0.0);
        debitLog.setAdminCommission(0.0);
        debitLog.setSuperAdminCommission(0.0);
        debitLog.setIsWinPayoutClaimed(false);
        debitLog.setIsAgentSettled(true);
        debitLog.setIsAdminSettled(true);
        debitLog.setIsSuperAdminSettled(true);
        debitLog.setAgentSettledAt(now);
        debitLog.setAdminSettledAt(now);
        debitLog.setSuperAdminSettledAt(now);
        debitLog.setTransactionId(UUID.randomUUID().toString());
        debitLog.setExplanation("TRANSFER (DEBIT): $" + request.getAmount() + " sent to " + toRole + " [ID: " + receiverWallet.getUserId() + "]");
        debitLog.setPreviousBalance(prevSenderBalance);
        debitLog.setCurrentBalance(senderWallet.getBalance());

        transactionLogDao.save(debitLog);

        // 🔹 Receiver CREDIT log
        TransactionLogEntity creditLog = new TransactionLogEntity();
        creditLog.setAgentId(toRole == RoleTypes.AGENT ? receiverWallet.getUserId() : null);
        creditLog.setAdminId(toRole == RoleTypes.ADMIN ? receiverWallet.getUserId() : null);
        creditLog.setSuperAdminId(toRole == RoleTypes.SUPER_ADMIN ? receiverWallet.getUserId() : null);
        creditLog.setTransactionType(TransactionType.TRANSFER);
        creditLog.setBetAmount(request.getAmount());
        creditLog.setIsWin(null);
        creditLog.setWinAmount(null);
        creditLog.setAgentCommission(0.0);
        creditLog.setAdminCommission(0.0);
        creditLog.setSuperAdminCommission(0.0);
        creditLog.setIsWinPayoutClaimed(false);
        creditLog.setIsAgentSettled(true);
        creditLog.setIsAdminSettled(true);
        creditLog.setIsSuperAdminSettled(true);
        creditLog.setAgentSettledAt(now);
        creditLog.setAdminSettledAt(now);
        creditLog.setSuperAdminSettledAt(now);
        creditLog.setTransactionId(UUID.randomUUID().toString());
        creditLog.setExplanation("TRANSFER (CREDIT): $" + request.getAmount() + " received from " + fromRole + " [ID: " + senderWallet.getUserId() + "]");
        creditLog.setPreviousBalance(prevReceiverBalance);
        creditLog.setCurrentBalance(receiverWallet.getBalance());

        transactionLogDao.save(creditLog);
    }

    public void rejectSettlement(Long requestId) {
        Long currentUserId = userContext.getUserId();
        OutstandingEntity request = outstandingDao.findById(requestId)
                .orElseThrow(() -> new DataValidationException("Request not found"));

        if (!request.getToUserId().equals(currentUserId)) {
            throw new DataValidationException("Unauthorized reject");
        }

        if (!request.getStatus().equals(TransactionApprovalStatus.PENDING)) {
            throw new DataValidationException("Request already resolved");
        }

        request.setStatus(TransactionApprovalStatus.REJECTED);
        request.setResolvedAt(new Date());
        outstandingDao.save(request);
    }

    private RoleTypes getUserRole(Long userId) {
        if (agentDao.existsById(userId)) return RoleTypes.AGENT;
        if (adminDao.existsById(userId)) return RoleTypes.ADMIN;
        if (superAdminDao.existsById(userId)) return RoleTypes.SUPER_ADMIN;
        throw new DataValidationException("User not found in any role");
    }

    public ResponseDto getMyWalletOverview() {
        Long userId = userContext.getUserId();
        RoleTypes role = RoleTypes.valueOf(UserContext.getRole());
        if (role == RoleTypes.PLAYER) {
            return getWalletOverview(userId);
        }

        ManagementWalletEntity wallet = managementWalletDao.findByUserIdAndRole(userId, role)
                .orElseThrow(() -> new DataValidationException("Wallet not found"));

        WalletOverviewDto dto = new WalletOverviewDto();
        dto.setUserId(userId);
        dto.setRole(role);
        dto.setCurrentBalance(wallet.getBalance());
        dto.setTotalReceived(wallet.getTotalReceived());
        dto.setTotalSettled(wallet.getTotalSettled());

        // Balance owed = all pending outward requests
        List<OutstandingEntity> outstandings = outstandingDao.findByFromUserIdAndStatus(userId, TransactionApprovalStatus.PENDING);
        Double owed = outstandings
                .stream().mapToDouble(OutstandingEntity::getAmount).sum();

        // Who I owe (outgoing)
        List<OutstandingEntity> oweList = outstandingDao.findByFromUserIdAndStatus(userId, TransactionApprovalStatus.PENDING);
        List<OwedPartyDto> peopleIOwe = oweList.stream().map(o -> {
            String name;
            if (o.getRoleType() == RoleTypes.ADMIN) {
                name = adminDao.findById(o.getToUserId()).map(AdminEntity::getName).orElse("N/A");
            } else if (o.getRoleType() == RoleTypes.SUPER_ADMIN) {
                name = "Super Admin";
            } else {
                name = agentDao.findById(o.getToUserId()).map(AgentEntity::getName).orElse("N/A");
            }
            return new OwedPartyDto(o.getToUserId(), o.getRoleType(), name, o.getAmount());
        }).toList();

        // Who owes me (incoming)
        List<OutstandingEntity> owedToMeList = outstandingDao.findByToUserIdAndStatus(userId, TransactionApprovalStatus.PENDING);
        List<OwedPartyDto> peopleWhoOweMe = owedToMeList.stream().map(o -> {
            String name;
            if (o.getRoleType() == RoleTypes.ADMIN) {
                name = adminDao.findById(o.getFromUserId()).map(AdminEntity::getName).orElse("N/A");
            } else if (o.getRoleType() == RoleTypes.SUPER_ADMIN) {
                name = "Super Admin";
            } else {
                name = agentDao.findById(o.getFromUserId()).map(AgentEntity::getName).orElse("N/A");
            }
            return new OwedPartyDto(o.getFromUserId(), o.getRoleType(), name, o.getAmount());
        }).toList();

        // Set in DTO
        dto.setPeopleIOwe(peopleIOwe);
        dto.setPeopleWhoOweMe(peopleWhoOweMe);


        dto.setBalanceOwed(owed);
        dto.setUsableBalance(wallet.getBalance() - owed);
        dto.setVirtualBalance(owed);

        // Username
        switch (role) {
            case AGENT -> dto.setUserName(agentDao.findById(userId).map(AgentEntity::getName).orElse("N/A"));
            case ADMIN -> dto.setUserName(adminDao.findById(userId).map(AdminEntity::getName).orElse("N/A"));
            case SUPER_ADMIN -> dto.setUserName("Super Admin");
        }

        // Recent transactions
        List<TransactionLogEntity> logs = findTop10ByUser(userId, role, false);
        dto.setRecentTransactions(logs.stream().map(this::mapTransaction).toList());

        // Recent requests (incoming/outgoing)
        List<OutstandingEntity> requests = outstandingDao
                .findTop5ByFromUserIdOrToUserIdOrderByCreatedAtDesc(userId, userId);
        dto.setRecentRequests(requests.stream().map(this::mapToRequestDto).toList());

        // Wallet activity logs
        List<TransactionLogEntity> activityLogs = findTop10ByUser(userId, role, true);
        dto.setTopActivities(activityLogs.stream().map(this::mapToActivityDto).toList());

        return dto;
    }

    public PlayerWalletOverviewDto getWalletOverview(Long playerId) {
        PlayerEntity player = playerDao.findById(playerId)
                .orElseThrow(() -> new DataValidationException("Player not found"));

        WalletEntity wallet = walletDao.findByPlayerId(playerId)
                .orElseThrow(() -> new DataValidationException("Wallet not found"));

        PlayerWalletOverviewDto dto = new PlayerWalletOverviewDto();
        dto.setPlayerId(playerId);
        dto.setPlayerName(player.getName());
        dto.setCashBalance(wallet.getCashBalance());
        dto.setVirtualBalance(wallet.getVirtualBalance());
        dto.setTotalBalance(wallet.getCashBalance() + wallet.getVirtualBalance());
        dto.setTotalAddedByAgent(wallet.getTotalAddedByAgent());
        dto.setTotalWon(wallet.getTotalWon());
        dto.setTotalLost(wallet.getTotalLost());

        // Recent transactions (top 10)
        List<TransactionLogEntity> logs = transactionLogDao.findTop10ByCustomerIdOrderByCreatedAtDesc(playerId);
        dto.setRecentTransactions(logs.stream().map(this::mapTransaction).toList());

        // Wallet activity (optional: bets won/lost, withdrawals)
        List<TransactionLogEntity> activities = transactionLogDao.findTop10ByCustomerIdOrderByCreatedAtDesc(playerId);
        dto.setTopActivities(activities.stream().map(this::mapToActivityDto).toList());

        return dto;
    }

    public List<TransactionLogEntity> findTop10ByUser(Long userId, RoleTypes role, boolean isSorted) {
        return switch (role) {
            case AGENT -> isSorted ?
                    transactionLogDao.findTop10ByAgentIdOrderByCreatedAtDesc(userId) :
                    transactionLogDao.findTop10ByAgentId(userId);

            case ADMIN -> isSorted ?
                    transactionLogDao.findTop10ByAdminIdOrderByCreatedAtDesc(userId) :
                    transactionLogDao.findTop10ByAdminId(userId);

            case SUPER_ADMIN -> isSorted ?
                    transactionLogDao.findTop10BySuperAdminIdOrderByCreatedAtDesc(userId) :
                    transactionLogDao.findTop10BySuperAdminId(userId);

            default -> List.of();
        };
    }

    private SettlementRequestDto mapToRequestDto(OutstandingEntity entity) {
        SettlementRequestDto dto = new SettlementRequestDto();
        dto.setId(entity.getId());
        dto.setFromUserId(entity.getFromUserId());
        dto.setToUserId(entity.getToUserId());
        dto.setAmount(entity.getAmount());
        dto.setStatus(entity.getStatus());
        dto.setNote(entity.getNote());
        dto.setResolvedAt(entity.getResolvedAt());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }

    private TransactionLogDto mapTransaction(TransactionLogEntity entity) {
        TransactionLogDto dto = new TransactionLogDto();
        dto.setTransactionId(entity.getTransactionId());
        dto.setCustomerId(entity.getCustomerId());
        dto.setAgentId(entity.getAgentId());
        dto.setAdminId(entity.getAdminId());
        dto.setSuperAdminId(entity.getSuperAdminId());
        dto.setTransactionType(entity.getTransactionType());
        dto.setBetAmount(entity.getBetAmount());
        dto.setIsWin(entity.getIsWin());
        dto.setWinAmount(entity.getWinAmount());
        dto.setAgentCommission(entity.getAgentCommission());
        dto.setAdminCommission(entity.getAdminCommission());
        dto.setSuperAdminCommission(entity.getSuperAdminCommission());
        dto.setAgentSettled(entity.getIsAgentSettled());
        dto.setAdminSettled(entity.getIsAdminSettled());
        dto.setSuperAdminSettled(entity.getIsSuperAdminSettled());
        dto.setAgentSettledAt(entity.getAgentSettledAt());
        dto.setAdminSettledAt(entity.getAdminSettledAt());
        dto.setSuperAdminSettledAt(entity.getSuperAdminSettledAt());
        dto.setExplanation(entity.getExplanation());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }

    private WalletActivityDto mapToActivityDto(TransactionLogEntity log) {
        WalletActivityDto dto = new WalletActivityDto();
        dto.setTransactionId(log.getTransactionId());
        dto.setTransactionType(log.getTransactionType());
        dto.setDate(log.getCreatedAt());

        String summary = switch (log.getTransactionType()) {
            case DEPOSIT -> "Deposit: ₹" + log.getBetAmount();
            case WITHDRAW -> "Withdraw: ₹" + log.getBetAmount();
            case TRANSFER -> "Transfer: ₹" + log.getBetAmount();
            case TRANSFER_DEPOSIT -> "Transfer In: ₹" + log.getBetAmount();
            case TRANSFER_WITHDRAW -> "Transfer Out: ₹" + log.getBetAmount();
            case LOAN -> "Loan Disbursed: ₹" + log.getBetAmount();
            case LOAN_SETTLEMENT -> "Loan Settled: ₹" + log.getBetAmount();
            default -> "Txn ₹" + log.getBetAmount();
        };

        dto.setSummary(summary);
        return dto;
    }

    public Double getUsableBalanceForLoggedInUser() {
        String role = UserContext.getRole();
        Long userId = userContext.getUserId();

        RoleTypes roleEnum = RoleTypes.valueOf(role);
        ManagementWalletEntity wallet = managementWalletDao.findByUserIdAndRole(userId, roleEnum)
                .orElseThrow(() -> new DataValidationException("Wallet not found"));

        return wallet.getBalance();
    }

    public Double getUsableBalanceForUser(Long userId, RoleTypes role) {
        ManagementWalletEntity wallet = managementWalletDao.findByUserIdAndRole(userId, role)
                .orElseThrow(() -> new DataValidationException("Wallet not found"));
        return wallet.getBalance();
    }
}
