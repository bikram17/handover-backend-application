package com.arenabast.api.service;

import com.arenabast.api.auth.jwt.UserContext;
import com.arenabast.api.dao.AgentDao;
import com.arenabast.api.dao.WalletDao;
import com.arenabast.api.dao.WalletTransactionRequestDao;
import com.arenabast.api.dto.AgentDto;
import com.arenabast.api.dto.AgentEditRequestDto;
import com.arenabast.api.dto.WalletTransactionRequestEntity;
import com.arenabast.api.entity.AgentEntity;
import com.arenabast.api.entity.WalletEntity;
import com.arenabast.api.enums.TransactionApprovalStatus;
import com.arenabast.api.enums.TransactionType;
import com.arenabast.api.exception.DataValidationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class AgentService {

    private final AgentDao agentDao;
    private final WalletTransactionRequestDao walletTransactionRequestDao;
    private final WalletDao walletDao;
    private final UserContext userContext;
    private final PasswordEncoder passwordEncoder;

    public AgentService(final AgentDao agentDao, WalletTransactionRequestDao walletTransactionRequestDao, WalletDao walletDao, UserContext userContext, PasswordEncoder passwordEncoder) {
        this.agentDao = agentDao;
        this.walletTransactionRequestDao = walletTransactionRequestDao;
        this.walletDao = walletDao;
        this.userContext = userContext;
        this.passwordEncoder = passwordEncoder;
    }

    public List<AgentDto> getAgents() {
        return null;
    }


    public void approveWalletRequest(Long requestId) {
        WalletTransactionRequestEntity request = walletTransactionRequestDao.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));

        if (request.getTransactionType() == TransactionType.DEPOSIT) {
            WalletEntity wallet = walletDao.findByPlayerId(request.getPlayerId())
                    .orElseThrow(() -> new IllegalArgumentException("Wallet not found"));
            wallet.setCashBalance(wallet.getCashBalance() + request.getRequestedAmount());
            walletDao.save(wallet);
            request.setStatus(TransactionApprovalStatus.COMPLETED); // Deposits are completed here
            request.setResolvedAt(new Date());
        } else if (request.getTransactionType() == TransactionType.WITHDRAW) {
            request.setStatus(TransactionApprovalStatus.APPROVED_PENDING_CLAIM);
            // no wallet deduction yet
        }

        walletTransactionRequestDao.save(request);
    }

    public void updateAgent(Long agentId, AgentEditRequestDto dto) {
        String callerRole = UserContext.getRole();
        Long callerId = userContext.getUserId();

        AgentEntity agent = agentDao.findById(agentId)
                .orElseThrow(() -> new DataValidationException("Agent not found"));

        // ✅ Authorization check
        if ("ADMIN".equals(callerRole) && !agent.getAdminId().equals(callerId)) {
            throw new DataValidationException("Unauthorized to edit this agent");
        }

        if (dto.getName() != null) agent.setName(dto.getName());
        if (dto.getEmail() != null) agent.setEmail(dto.getEmail());
        if (dto.getPassword() != null) agent.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        if (dto.getActive() != null) agent.setActive(dto.getActive());

        agentDao.save(agent);
    }

    public void changeAgentStatus(Long agentId, boolean active) {
        String callerRole = UserContext.getRole();
        Long callerId = userContext.getUserId();

        AgentEntity agent = agentDao.findById(agentId)
                .orElseThrow(() -> new DataValidationException("Agent not found"));

        if ("ADMIN".equals(callerRole) && !agent.getAdminId().equals(callerId)) {
            throw new DataValidationException("Unauthorized to change status of this agent");
        }

        agent.setActive(active);
        agentDao.save(agent);
    }

}
