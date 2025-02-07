package com.example.clouddisk.security;

import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;

public class JwtAuth extends AbstractAuthenticationToken {
    private final Long userId;
    @Getter
    private final String role;

    public JwtAuth(Long userId, String role) {
        super(null);
        this.userId = userId;
        this.role = role;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return userId;
    }

}
