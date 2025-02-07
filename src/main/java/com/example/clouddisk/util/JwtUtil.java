package com.example.clouddisk.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

public class JwtUtil {
    public static final SecretKey key = Jwts.SIG.HS256.key().build();
    public static final int DURATION = 24 * 3600;

    public static String generateJWT(String username, Long userId, String role){
        return Jwts.builder()
                .subject(username)
                .claim("user_id", userId)
                .claim("user_role", role)
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusSeconds(DURATION)))
                .signWith(key)
                .compact();
    }

    public static Claims parsePayload(String jwt){
        try{
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(jwt)
                    .getPayload();
        }
        catch (Exception e){
            return null;
        }
    }

    public static String parseSubject(String jwt){
        Claims payload = parsePayload(jwt);
        if (payload == null) return null;
        return payload.getSubject();
    }

    public static Long parseUserId(String jwt){
        Claims payload = parsePayload(jwt);
        if (payload == null) return null;
        return payload.get("user_id", Long.class);
    }

    public static String parseRole(String jwt){
        Claims payload = parsePayload(jwt);
        if (payload == null) return null;
        return payload.get("user_role", String.class);
    }

}
