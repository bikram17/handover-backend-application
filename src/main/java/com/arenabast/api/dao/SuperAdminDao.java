package com.arenabast.api.dao;

import com.arenabast.api.entity.SuperAdminEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SuperAdminDao extends JpaRepository<SuperAdminEntity, Long> {
    Optional<SuperAdminEntity> findByEmail(String email);

    SuperAdminEntity findFirstByEmail(String email);

    @Query("select sae from SuperAdminEntity sae where sae.isCollector = true")
    Optional<SuperAdminEntity> findCollector();
}
