package com.example.clouddisk.security;

import com.example.clouddisk.util.JWUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@WebFilter
//@Order(1)
public class JwtAuthFilter extends OncePerRequestFilter {

    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }
        String token = authHeader.substring(7);
        String username = JWUtil.parseSubject(token);

        if (username != null) {
            var currAuth = SecurityContextHolder.getContext().getAuthentication();
            if (currAuth == null || !currAuth.getPrincipal().equals(username)) {
                SecurityContextHolder.clearContext();
                SecurityContextHolder.getContext().setAuthentication(new JwtAuth(username));
            }
        }
        chain.doFilter(request, response);
    }
}
