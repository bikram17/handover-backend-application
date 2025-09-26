package com.arenabast.api.auth.jwt;

import com.arenabast.api.dao.AdminDao;
import com.arenabast.api.dao.AgentDao;
import com.arenabast.api.dao.PlayerDao;
import com.arenabast.api.dao.SuperAdminDao;
import com.arenabast.api.exception.DataValidationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class UserContext {
    private final SuperAdminDao superAdminDao;
    private final AdminDao adminDao;
    private final AgentDao agentDao;
    private final PlayerDao playerDao;

    public UserContext(SuperAdminDao superAdminDao, AdminDao adminDao, AgentDao agentDao, PlayerDao playerDao) {
        this.superAdminDao = superAdminDao;
        this.adminDao = adminDao;
        this.agentDao = agentDao;
        this.playerDao = playerDao;
    }

    public static String getEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }

    public static String getUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }

    public static String getRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.getAuthorities().isEmpty()) return null;
        return authentication.getAuthorities().iterator().next().getAuthority();
    }

    public Long getUserId() {
        String role = getRole();

        if (role == null) throw new DataValidationException("No role found");

        if (role.equals("SUPER_ADMIN")) return superAdminDao.findFirstByEmail(getEmail()).getId();

        if (role.equals("ADMIN")) return adminDao.findByUserName(getEmail()).get().getId();

        if (role.equals("AGENT")) return agentDao.findByUserName(getEmail()).get().getId();

        if (role.equals("PLAYER")) return playerDao.findByUserName(getEmail()).get().getId();

        throw new DataValidationException("No role present with the associated user");
    }
}