package com.arenabast.api.service;

import com.arenabast.api.dao.*;
import com.arenabast.api.dto.SimulateBetRequest;
import com.arenabast.api.entity.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionSimulationService {

    private final PlayerDao playerDao;
    private final AgentDao agentDao;
    private final AdminDao adminDao;
    private final SuperAdminDao superAdminDao;
    private final WalletDao walletDao;
    private final TransactionLogDao transactionLogDao;

    public TransactionLogEntity simulateBetOutcome(SimulateBetRequest request) {
        PlayerEntity player = playerDao.findById(request.getPlayerId())
                .orElseThrow(() -> new IllegalArgumentException("Player not found"));

        AgentEntity agent = agentDao.findById(player.getAgentId())
                .orElseThrow(() -> new IllegalArgumentException("Agent not found"));

        AdminEntity admin = adminDao.findById(agent.getAdminId())
                .orElseThrow(() -> new IllegalArgumentException("Admin not found"));

        SuperAdminEntity superAdmin = superAdminDao.findCollector()
                .orElseThrow(() -> new IllegalStateException("Super Admin not found"));

        WalletEntity wallet = walletDao.findByPlayerId(player.getId())
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found"));

        String txnId = UUID.randomUUID().toString();
        TransactionLogEntity log = new TransactionLogEntity();

        log.setTransactionId(txnId);
        log.setCustomerId(player.getId());
        log.setAgentId(agent.getId());
        log.setAdminId(admin.getId());
        log.setSuperAdminId(superAdmin.getId());
        log.setIsWin(request.getIsWin());
        log.setBetAmount(request.getAmount());

        if (request.getIsWin()) {
            // Player wins: $1000 -> $100 cash, $900 virtual
            double winAmount = request.getAmount() * 10;
            wallet.setCashBalance(wallet.getCashBalance() + 100);
            wallet.setVirtualBalance(wallet.getVirtualBalance() + (winAmount - 100));
            wallet.setTotalWon(wallet.getTotalWon() + winAmount);

            log.setWinAmount(winAmount);
            log.setExplanation("WIN: Player won $" + winAmount + " ($100 cash + $" + (winAmount - 100) + " virtual)");
        } else {
            double amount = request.getAmount();
            double agentCut = amount * 0.30;
            double adminCut = (amount - agentCut) * 0.20;
            double superCut = amount - agentCut - adminCut;

            wallet.setTotalLost(wallet.getTotalLost() + amount);

            log.setAgentCommission(agentCut);
            log.setAdminCommission(adminCut);
            log.setSuperAdminCommission(superCut);
            log.setIsAgentSettled(false);
            log.setIsAdminSettled(false);
            log.setIsSuperAdminSettled(false);

            log.setExplanation("LOSS: Agent gets $" + agentCut + ", Admin gets $" + adminCut + ", SuperAdmin gets $" + superCut);
        }

        walletDao.save(wallet);
        return transactionLogDao.save(log);
    }
}
