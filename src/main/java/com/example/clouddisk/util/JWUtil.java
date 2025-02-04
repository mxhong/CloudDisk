package com.example.clouddisk.util;

import io.jsonwebtoken.Jwts;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

public class JWUtil {
    public static final SecretKey key = Jwts.SIG.HS256.key().build();
    public static final int DURATION = 24 * 3600;

    public static String generateJWT(String username){
        return Jwts.builder()
                .subject(username)
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusSeconds(DURATION)))
                .signWith(key)
                .compact();
    }

    public static String parseSubject(String token){
        try{
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
        }
        catch (Exception e){
            return null;
        }
    }
}
