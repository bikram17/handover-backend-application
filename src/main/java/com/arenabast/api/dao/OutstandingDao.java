package com.arenabast.api.dao;

import com.arenabast.api.entity.OutstandingEntity;
import com.arenabast.api.enums.TransactionApprovalStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutstandingDao extends JpaRepository<OutstandingEntity, Long> {
    List<OutstandingEntity> findByToUserIdAndStatus(Long userId, TransactionApprovalStatus transactionApprovalStatus);

    List<OutstandingEntity> findByToUserId(Long userId);

    List<OutstandingEntity> findByFromUserIdAndStatus(Long userId, TransactionApprovalStatus transactionApprovalStatus);

    List<OutstandingEntity> findTop5ByFromUserIdOrToUserIdOrderByCreatedAtDesc(Long userId, Long userId1);
}
