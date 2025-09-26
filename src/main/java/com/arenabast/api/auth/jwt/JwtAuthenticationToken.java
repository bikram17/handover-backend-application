package com.arenabast.api.auth.jwt;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;

public class JwtAuthenticationToken extends AbstractAuthenticationToken {

    private final String userEmail;
    private final String role;

    public JwtAuthenticationToken(String userEmail, String role) {
        super(Collections.singleton(new SimpleGrantedAuthority("ROLE_" + role)));
        this.userEmail = userEmail;
        this.role = role;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return userEmail;
    }

    public String getRole() {
        return role;
    }
}