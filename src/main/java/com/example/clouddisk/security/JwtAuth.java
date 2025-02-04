package com.example.clouddisk.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;

public class JwtAuth extends AbstractAuthenticationToken {
    private final String username;

    public JwtAuth(String username) {
        super(null);
        this.username = username;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return username;
    }
}
