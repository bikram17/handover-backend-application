package com.arenabast.api.service;

import com.arenabast.api.dao.*;
import com.arenabast.api.dto.BetDao;
import com.arenabast.api.dto.BetFilterRequest;
import com.arenabast.api.dto.PlaceBetRequest;
import com.arenabast.api.entity.*;
import com.arenabast.api.enums.BetResult;
import com.arenabast.api.enums.BetStatus;
import com.arenabast.api.enums.TransactionApprovalStatus;
import com.arenabast.api.exception.DataValidationException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Service
@Slf4j
@Transactional
public class BetService {

    private final WalletDao walletDao;
    private final BetDao betDao;
    private final OutstandingDao outstandingDao;
    private final PlayerDao playerDao;
    private final AgentDao agentDao;
    private final AdminDao adminDao;

    public BetService(WalletDao walletDao, BetDao betDao, OutstandingDao outstandingDao, PlayerDao playerDao, AgentDao agentDao, AdminDao adminDao) {
        this.walletDao = walletDao;
        this.betDao = betDao;
        this.outstandingDao = outstandingDao;
        this.playerDao = playerDao;
        this.agentDao = agentDao;
        this.adminDao = adminDao;
    }

    public BetEntity placeBet(Long playerId, PlaceBetRequest request) {
        WalletEntity wallet = walletDao.findByPlayerId(playerId)
                .orElseThrow(() -> new DataValidationException("Wallet not found"));

        double remainingStake = request.getStake();

        // Deduct from cash first
        if (wallet.getCashBalance() >= remainingStake) {
            wallet.setCashBalance(wallet.getCashBalance() - remainingStake);
            remainingStake = 0;
        } else {
            remainingStake -= wallet.getCashBalance();
            wallet.setCashBalance(0.0);
        }

        // Deduct remaining from virtual
        if (remainingStake > 0) {
            if (wallet.getVirtualBalance() < remainingStake) {
                throw new DataValidationException("Insufficient balance");
            }
            wallet.setVirtualBalance(wallet.getVirtualBalance() - remainingStake);
        }

        walletDao.save(wallet);

        BetEntity bet = new BetEntity();
        bet.setPlayerId(playerId);
        bet.setSportKey(request.getSportKey());
        bet.setBetTeam(request.getBetTeam());
        bet.setEventId(request.getEventId());
        bet.setMarket(request.getMarket());
        bet.setSelection(request.getSelection());
        bet.setOdds(request.getOdds());
        bet.setStake(request.getStake());
        bet.setPotentialWin(request.getStake() * request.getOdds());
        bet.setStatus(BetStatus.PENDING);
        bet.setPlacedAt(new Date());

        return betDao.save(bet);
    }

    @Transactional
    public BetEntity settleBet(Long betId, BetResult result, Long playerId) {
        BetEntity bet = betDao.findById(betId)
                .orElseThrow(() -> new DataValidationException("Bet not found"));

        WalletEntity wallet = walletDao.findByPlayerId(bet.getPlayerId())
                .orElseThrow(() -> new DataValidationException("Wallet not found"));

        bet.setResult(result);
        bet.setSettledAt(new Date());

        if (result == BetResult.WIN) {
            double payout = bet.getPotentialWin();
            wallet.setVirtualBalance(wallet.getVirtualBalance() + payout);
            wallet.setTotalWon(wallet.getTotalWon() + payout);
        } else if (result == BetResult.LOSS) {
            double loss = bet.getStake();
            wallet.setTotalLost(wallet.getTotalLost() + loss);

            // Commission Split
            double agentCut = loss * 0.30;
            double adminCut = (loss - agentCut) * 0.20;
            double superCut = loss - agentCut - adminCut;

            PlayerEntity player = playerDao.findById(playerId).orElseThrow(() -> new DataValidationException("Player not found"));
            AgentEntity agentEntity = agentDao.findFirstById(player.getAgentId());
            AdminEntity admin = adminDao.findFirstById(agentEntity.getAdminId());
            Long superAdminId = 1L;

            // Outstanding flows
            OutstandingEntity agentOutstanding = new OutstandingEntity();
            agentOutstanding.setFromUserId(bet.getPlayerId());
            agentOutstanding.setToUserId(agentEntity.getId());
            agentOutstanding.setAmount(agentCut);
            agentOutstanding.setStatus(TransactionApprovalStatus.PENDING);
            outstandingDao.save(agentOutstanding);

            OutstandingEntity adminOutstanding = new OutstandingEntity();
            adminOutstanding.setFromUserId(agentEntity.getId());
            adminOutstanding.setToUserId(admin.getId());
            adminOutstanding.setAmount(adminCut);
            adminOutstanding.setStatus(TransactionApprovalStatus.PENDING);
            outstandingDao.save(adminOutstanding);

            OutstandingEntity superOutstanding = new OutstandingEntity();
            superOutstanding.setFromUserId(admin.getId());
            superOutstanding.setToUserId(superAdminId);
            superOutstanding.setAmount(superCut);
            superOutstanding.setStatus(TransactionApprovalStatus.PENDING);
            outstandingDao.save(superOutstanding);
        }

        walletDao.save(wallet);
        return betDao.save(bet);
    }

    public List<BetEntity> getPlayerBets(Long playerId) {
        // Ensure player exists
        playerDao.findById(playerId)
                .orElseThrow(() -> new DataValidationException("Player not found with id: " + playerId));

        return betDao.findByPlayerId(playerId);
    }

    public Page<BetEntity> getAllBets(BetFilterRequest filter, Pageable pageable) {
        List<Specification<BetEntity>> specs = new ArrayList<>();

        if (filter.getPlayerId() != null) {
            specs.add((root, query, cb) -> cb.equal(root.get("playerId"), filter.getPlayerId()));
        }
        if (filter.getStatus() != null) {
            specs.add((root, query, cb) -> cb.equal(root.get("status"), filter.getStatus()));
        }
        if (filter.getResult() != null) {
            specs.add((root, query, cb) -> cb.equal(root.get("result"), filter.getResult()));
        }
        if (filter.getFromDate() != null) {
            specs.add((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("placedAt"), filter.getFromDate()));
        }
        if (filter.getToDate() != null) {
            specs.add((root, query, cb) -> cb.lessThanOrEqualTo(root.get("placedAt"), filter.getToDate()));
        }
        // Add optional filters for eventId and sportKey
        if (filter.getEventId() != null) {
            specs.add((root, query, cb) -> cb.equal(root.get("eventId"), filter.getEventId()));
        }
        if (filter.getSportKey() != null) {
            specs.add((root, query, cb) -> cb.equal(root.get("sportKey"), filter.getSportKey()));
        }

        Specification<BetEntity> spec = Specification.allOf(specs);

        Pageable pageToUse = pageable;
        pageable.getSort();
        if (pageable.getSort().isUnsorted()) {
            pageToUse = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by("createdAt").descending());
        }

        return betDao.findAll(spec, pageToUse);
    }
}
