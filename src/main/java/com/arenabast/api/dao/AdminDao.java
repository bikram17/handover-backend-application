package com.arenabast.api.dao;

import com.arenabast.api.entity.AdminEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminDao extends JpaRepository<AdminEntity, Long> {
    Optional<AdminEntity> findByEmail(String email);

    AdminEntity findFirstByEmail(String email);

    AdminEntity findFirstById(Long adminId);

    Optional<AdminEntity> findByName(String email);

    Optional<AdminEntity> findByUserName(String userName);

    long countByActive(boolean b);
}
