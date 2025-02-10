package com.example.clouddisk.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class JwtUtil {
    private final SecretKey key = Jwts.SIG.HS256.key().build();
    private final long ACCESS_DURATION = 60 * 60; // 60 minutes
    private final long REFRESH_DURATION = 7 * 24 * 60; // 7 days

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    public String generateJWT(Long userId, String role){
        String jtiRaw = userId + "-" + System.currentTimeMillis();
        return Jwts.builder()
                .id(UUID.nameUUIDFromBytes(jtiRaw.getBytes()).toString())
                .subject(String.valueOf(userId))
                .claim("role", role)
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusSeconds(ACCESS_DURATION)))
                .signWith(key)
                .compact();
    }

    public String generateRefreshJwt(Long userId, String role){
        String jtiRaw = userId + "-" + System.currentTimeMillis();
        return Jwts.builder()
                .id(UUID.nameUUIDFromBytes(jtiRaw.getBytes()).toString())
                .subject(String.valueOf(userId))
                .claim("role", role)
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusSeconds(REFRESH_DURATION)))
                .signWith(key)
                .compact();
    }

    public Claims parsePayload(String jwt){
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

    public Long parseSubject(String jwt){
        Claims payload = parsePayload(jwt);
        return parseSubject(payload);
    }

    public String parseRole(String jwt){
        Claims payload = parsePayload(jwt);
        return parseRole(payload);
    }

    public Long getRemainingTime(String jwt){
        Claims payload = parsePayload(jwt);
        return getRemainingTime(payload);
    }

    public Long parseSubject(Claims payload){
        if (payload == null) return null;
        return Long.parseLong(payload.getSubject());
    }

    public String parseRole(Claims payload){
        if (payload == null) return null;
        return payload.get("role", String.class);
    }

    public Long getRemainingTime(Claims payload){
        if (payload == null) return 0L;
        return payload.getExpiration().getTime() - System.currentTimeMillis();
    }

    public void addBlackList(String jwt){
        Claims payload = parsePayload(jwt);
        if (payload != null) {
            addBlackList(payload.getId(), getRemainingTime(payload));
        }
    }

    public void addBlackList(String jti, Long remainingTime){
        stringRedisTemplate.opsForValue().set("blacklist: " + jti, "true",
                remainingTime / 1000, TimeUnit.SECONDS);
    }

    public boolean isBlackListed(String jwt){
        Claims payload = parsePayload(jwt);
        return (payload == null || stringRedisTemplate.hasKey("blacklist: " + payload.getId()));
    }

    public boolean isBlackListed(Claims payload){
        return (payload == null || stringRedisTemplate.hasKey("blacklist: " + payload.getId()));
    }

}
