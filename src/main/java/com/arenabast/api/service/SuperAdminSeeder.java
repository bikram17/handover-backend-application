package com.arenabast.api.service;

import com.arenabast.api.dao.SuperAdminDao;
import com.arenabast.api.entity.SuperAdminEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SuperAdminSeeder implements CommandLineRunner {

    private final SuperAdminDao superAdminDao;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (superAdminDao.count() == 0) {
            List<SuperAdminEntity> admins = new ArrayList<>();
            SuperAdminEntity admin = new SuperAdminEntity();
            admin.setName("Super Admin");
            admin.setEmail("admin@example.com");
            admin.setPasswordHash(passwordEncoder.encode("admin123"));

            SuperAdminEntity admin2 = new SuperAdminEntity();
            admin2.setName("Bikram Mitra");
            admin2.setEmail("bikramitra17@gmail.com");
            admin2.setPasswordHash(passwordEncoder.encode("bikram123"));

            admins.add(admin);
            admins.add(admin2);
            superAdminDao.saveAll(admins);
        }
    }
}