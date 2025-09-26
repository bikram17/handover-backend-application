package com.arenabast.api.service;

import com.arenabast.api.auth.jwt.UserContext;
import com.arenabast.api.dao.TransactionLogDao;
import com.arenabast.api.dto.TransactionLogDto;
import com.arenabast.api.entity.TransactionLogEntity;
import com.arenabast.api.enums.RoleTypes;
import com.arenabast.api.exception.DataValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class TransactionQueryService {

    private final TransactionLogDao transactionLogDao;
    private final UserContext userContext;

    public Page<TransactionLogDto> getAll(Pageable pageable) {
        return transactionLogDao.findAll(pageable)
                .map(this::mapToDto);
    }

    public Page<TransactionLogDto> getByAdmin(Long adminId, Pageable pageable) {
        return transactionLogDao.findByAdminId(adminId, pageable)
                .map(this::mapToDto);
    }

    public Page<TransactionLogDto> getByAgent(Long agentId, Pageable pageable) {
        return transactionLogDao.findByAgentId(agentId, pageable)
                .map(this::mapToDto);
    }

    private TransactionLogDto mapToDto(TransactionLogEntity entity) {
        TransactionLogDto dto = new TransactionLogDto();
        dto.setTransactionId(entity.getTransactionId());
        dto.setCustomerId(entity.getCustomerId());
        dto.setAgentId(entity.getAgentId());
        dto.setAdminId(entity.getAdminId());
        dto.setIsWin(entity.getIsWin());
        dto.setBetAmount(entity.getBetAmount());
        dto.setWinAmount(entity.getWinAmount());
        dto.setAgentCommission(entity.getAgentCommission());
        dto.setAdminCommission(entity.getAdminCommission());
        dto.setCurrentBalance(entity.getCurrentBalance());
        dto.setPreviousBalance(entity.getPreviousBalance());
        dto.setSuperAdminCommission(entity.getSuperAdminCommission());
        dto.setTransactionType(entity.getTransactionType());
        dto.setExplanation(entity.getExplanation());
        dto.setAgentSettled(entity.getIsAgentSettled());
        dto.setAdminSettled(entity.getIsAdminSettled());
        dto.setSuperAdminSettled(entity.getIsSuperAdminSettled());
        return dto;
    }

    public Page<TransactionLogDto> getMyTransactionLogsSorted(int page, int size) {
        Long userId = userContext.getUserId();
        RoleTypes role = RoleTypes.valueOf(UserContext.getRole());

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<TransactionLogEntity> logsPage = switch (role) {
            case SUPER_ADMIN -> transactionLogDao.findBySuperAdminId(userId, pageable);
            case ADMIN -> transactionLogDao.findByAdminId(userId, pageable);
            case AGENT -> transactionLogDao.findByAgentId(userId, pageable);
            case PLAYER -> transactionLogDao.findByCustomerId(userId, pageable);
            default -> throw new DataValidationException("Not supported for this role");
        };

        return logsPage.map(this::mapTransaction);
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
        dto.setTransactionType(e.getTransactionType());
        dto.setAdminCommission(e.getAdminCommission());
        dto.setSuperAdminCommission(e.getSuperAdminCommission());
        dto.setExplanation(e.getExplanation());
        dto.setAgentSettled(e.getIsAgentSettled());
        dto.setAdminSettled(e.getIsAdminSettled());
        dto.setSuperAdminSettled(e.getIsSuperAdminSettled());
        return dto;
    }

    public Page<TransactionLogDto> getByPlayer(Long playerId, Pageable pageable) {
        return transactionLogDao.findByAgentId(playerId, pageable)
                .map(this::mapToDto);
    }
}