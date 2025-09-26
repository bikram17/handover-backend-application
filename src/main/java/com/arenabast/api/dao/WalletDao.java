package com.arenabast.api.dao;

import com.arenabast.api.dto.WalletBalanceDto;
import com.arenabast.api.entity.WalletEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface WalletDao extends JpaRepository<WalletEntity, Long> {
    boolean existsByPlayerId(Long id);

    Optional<WalletEntity> findByPlayerId(Long playerId);

    List<WalletEntity> findByPlayerIdIn(List<Long> playerIds);

    WalletBalanceDto findSumBy();


    @Query("SELECT new com.arenabast.api.dto.WalletBalanceDto(" +
            "CAST(COALESCE(SUM(w.cashBalance),0) AS double), " +
            "CAST(COALESCE(SUM(w.virtualBalance),0) AS double)) " +
            "FROM WalletEntity w")
    WalletBalanceDto getTotalPlayerBalances();


//    List<WalletEntity> findAllByAgentId(Long agentIdParam);

//    List<WalletEntity> findAllPlayers();

//    List<WalletEntity> findByAgentId(Long callerId);
}
