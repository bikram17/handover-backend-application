package com.arenabast.api.service;

import com.arenabast.api.auth.jwt.JwtUtil;
import com.arenabast.api.dao.AdminDao;
import com.arenabast.api.dao.AgentDao;
import com.arenabast.api.dao.SuperAdminDao;
import com.arenabast.api.dto.login.LoginRequestDto;
import com.arenabast.api.dto.login.LoginResponseDto;
import com.arenabast.api.entity.AdminEntity;
import com.arenabast.api.entity.AgentEntity;
import com.arenabast.api.entity.SuperAdminEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class AuthService {


    @Autowired
    private SuperAdminDao superAdminRepo;
    @Autowired
    private AdminDao adminRepo;
    @Autowired
    private AgentDao agentRepo;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtil jwtUtil;

    public LoginResponseDto login(LoginRequestDto request) {
        log.info("Login request: {}", request);
        Optional<SuperAdminEntity> superAdmin = superAdminRepo.findByEmail(request.getEmail());
        if (superAdmin.isPresent()) {
            SuperAdminEntity user = superAdmin.get();
            if (passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
                String token = jwtUtil.generateToken(user.getEmail(), "SUPER_ADMIN");
                return new LoginResponseDto("SUPER_ADMIN", user.getId(), token, user.getEmail(), user.getUserName());
            }
        }
        log.info("Login failed");
        Optional<AdminEntity> admin = adminRepo.findByUserName(request.getEmail());
        if (admin.isPresent()) {
            AdminEntity user = admin.get();
            if (passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
                String token = jwtUtil.generateToken(user.getUserName(), "ADMIN");
                return new LoginResponseDto("ADMIN", user.getId(), token, user.getEmail(), user.getUserName());
            }
        }

        Optional<AgentEntity> agent = agentRepo.findByUserName(request.getEmail());
        if (agent.isPresent()) {
            AgentEntity user = agent.get();
            if (passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
                String token = jwtUtil.generateToken(user.getUserName(), "AGENT");
                return new LoginResponseDto("AGENT", user.getId(), token, user.getEmail(), user.getUserName());
            }
        }

        throw new RuntimeException("Invalid credentials");
    }
}
