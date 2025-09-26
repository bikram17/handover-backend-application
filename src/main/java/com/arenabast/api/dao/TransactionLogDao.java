package com.arenabast.api.dao;

import com.arenabast.api.entity.TransactionLogEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

import java.util.List;

public interface TransactionLogDao extends JpaRepository<TransactionLogEntity, Long> {

    @NonNull
    Page<TransactionLogEntity> findAll(@NonNull Pageable pageable);

    Page<TransactionLogEntity> findByAdminId(Long adminId, Pageable pageable);

    Page<TransactionLogEntity> findByAgentId(Long agentId, Pageable pageable);

    List<TransactionLogEntity> findTop10ByCustomerIdOrderByCreatedAtDesc(Long playerId);

    List<TransactionLogEntity> findTop10ByAgentIdOrderByCreatedAtDesc(Long agentId);

    List<TransactionLogEntity> findTop10ByAdminIdOrderByCreatedAtDesc(Long adminId);

    List<TransactionLogEntity> findTop10BySuperAdminIdOrderByCreatedAtDesc(Long superAdminId);

    List<TransactionLogEntity> findTop10ByAgentId(Long userId);

    List<TransactionLogEntity> findTop10ByAdminId(Long userId);

    List<TransactionLogEntity> findTop10BySuperAdminId(Long userId);

    List<TransactionLogEntity> findBySuperAdminIdOrderByCreatedAtDesc(Long userId);

    List<TransactionLogEntity> findByAdminIdOrderByCreatedAtDesc(Long userId);

    List<TransactionLogEntity> findByAgentIdOrderByCreatedAtDesc(Long userId);

    Page<TransactionLogEntity> findBySuperAdminId(Long superAdminId, Pageable pageable);

    Page<TransactionLogEntity> findByCustomerId(Long userId, Pageable pageable);
}
