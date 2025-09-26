package com.arenabast.api.dao;

import com.arenabast.api.entity.AgentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AgentDao extends JpaRepository<AgentEntity, Long> {
    Optional<AgentEntity> findByEmail(String email);

    Optional<AgentEntity> findFirstByEmail(String email);

    AgentEntity findFirstById(Long adminId);

    List<AgentEntity> findByAdminId(Long callerId);

    Page<AgentEntity> findByAdminId(Long callerId, Pageable pageable);

    Optional<AgentEntity> findByName(String name);

    int countByAdminId(Long id);

    Optional<AgentEntity> findByUserName(String email);

    long countByAdminIdAndActive(Long callerId, boolean b);

    long countByActive(boolean b);
}
