package com.arenabast.api.dto;

import com.arenabast.api.entity.BetEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BetDao extends JpaRepository<BetEntity, Long> {
    List<BetEntity> findByPlayerId(Long playerId);

    Page<BetEntity> findAll(Specification<BetEntity> spec, Pageable pageToUse);
}
