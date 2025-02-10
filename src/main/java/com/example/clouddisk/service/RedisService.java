package com.example.clouddisk.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public RedisService(RedisTemplate<String, Object> redisTemplate,
                        ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public <T> void set(String key, T value) {
        redisTemplate.opsForValue().set(key, value);
    }

    public <T> void set(String key, T value, long timeout) {
        redisTemplate.opsForValue().set(key, value, timeout, TimeUnit.SECONDS);
    }

    public <T> T get(String key, TypeReference<T> typeReference) {
        try{
            Object value = redisTemplate.opsForValue().get(key);
            return value == null ? null : objectMapper.convertValue(value, typeReference);
        }
        catch (IllegalArgumentException e){
            throw new RuntimeException("Failed to deserialize value for key: " + key, e);
        }
    }


    public void delete(String key) {
        redisTemplate.delete(key);
    }
}
