package com.gnwoo.apigateway.data.repo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Repository
public class WsSessionRepo {
    @Autowired
    @Qualifier("RedisStringLongTemplate")
    RedisTemplate<String, Long> redisTemplate;

    public String establishSession(Long uuid) {
        String session_token = UUID.randomUUID().toString();
        try {
            redisTemplate.opsForValue().set(session_token, uuid);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return session_token;
    }

    public Long findSessionByToken(String session_token) {
        return redisTemplate.opsForValue().get(session_token);
    }

    public void deleteSessionByToken(String session_token) {
        redisTemplate.delete(session_token);
    }
}
