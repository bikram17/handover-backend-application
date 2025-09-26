package com.arenabast.api.dao;

import com.arenabast.api.dto.WalletTransactionRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletTransactionRequestDao extends JpaRepository<WalletTransactionRequestEntity, Long> {
}
