package com.arenabast.api.dao;

import com.arenabast.api.entity.PlayerEntity;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlayerDao extends JpaRepository<PlayerEntity, Long> {
    boolean existsByEmail(String email);

    List<PlayerEntity> findByAgentIdIn(List<Long> agentIds);

    List<PlayerEntity> findByAgentId(Long callerId);

    int countByAgentId(Long id);

    Page<PlayerEntity> findByAgentId(Long agentIdParam, Pageable pageable);

    Page<PlayerEntity> findByAgentIdIn(List<Long> agentIds, Pageable pageable);

    long countByActive(boolean b);

    boolean existsByUserName(@NotBlank String userName);

    Page<PlayerEntity> findByAgentIdAndActive(Long agentIdParam, Pageable pageable, Boolean active);

    Page<PlayerEntity> findByActive(Boolean active, Pageable pageable);

    Page<PlayerEntity> findByAgentIdInAndActive(List<Long> agentIds, Boolean active, Pageable pageable);

    Optional<PlayerEntity> findByUserName(String username);
}
