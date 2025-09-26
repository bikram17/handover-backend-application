package com.arenabast.api.dao;

import com.arenabast.api.dto.TopAdminWalletDto;
import com.arenabast.api.dto.TopAgentsWalletDto;
import com.arenabast.api.entity.ManagementWalletEntity;
import com.arenabast.api.enums.RoleTypes;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ManagementWalletDao extends JpaRepository<ManagementWalletEntity, Long> {
    Optional<ManagementWalletEntity> findByUserIdAndRole(Long userId, RoleTypes role);

    List<ManagementWalletEntity> findByRole(RoleTypes role);

    List<ManagementWalletEntity> findByUserIdInAndRole(List<Long> ids, RoleTypes role);

    @Query("SELECT SUM(w.cashBalance), SUM(w.virtualBalance) FROM WalletEntity w")
    Object[] getTotalPlayerBalances();

    @Query("SELECT SUM(w.balance) FROM ManagementWalletEntity w WHERE w.role = :role")
    Double getTotalBalanceByRole(@Param("role") RoleTypes role);

    @Query("SELECT new com.arenabast.api.dto.TopAdminWalletDto(a.id, a.name, w.balance) " +
            "FROM AdminEntity a JOIN ManagementWalletEntity w ON a.id = w.userId " +
            "WHERE w.role = 'ADMIN' ORDER BY w.balance DESC")
    List<TopAdminWalletDto> getTopAdmins(Pageable pageable);

    @Query("SELECT new com.arenabast.api.dto.TopAgentsWalletDto(ag.id, ag.name, w.balance) " +
            "FROM AgentEntity ag JOIN ManagementWalletEntity w ON ag.id = w.userId " +
            "WHERE w.role = 'AGENT' ORDER BY w.balance DESC")
    List<TopAgentsWalletDto> getTopAgents(Pageable pageable);

}
