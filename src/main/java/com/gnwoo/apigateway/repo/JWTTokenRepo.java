package com.gnwoo.apigateway.repo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Repository
public class JWTTokenRepo {
    @Autowired
    RedisTemplate<Long, String> redisTemplate;

    public Boolean saveJWTToken(Long uuid, String JWT_token) {
        try
        {
            redisTemplate.opsForList().leftPush(uuid, JWT_token);
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public String get(Long uuid, String JWT_token) {
        Long size = redisTemplate.opsForList().size(uuid);
        if (Objects.isNull(size))
            return null;
        List<String> tokens = redisTemplate.opsForList().range(uuid, 0, size);
        if (Objects.isNull(tokens) || tokens.isEmpty() || !tokens.contains(JWT_token))
            return null;
        return tokens.get(tokens.indexOf(JWT_token));
    }

    public boolean remove(Long uuid, String JWT_token) {
        try
        {
            redisTemplate.opsForList().remove(uuid, 0, JWT_token);
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    public boolean removeAll(Long uuid) {
        try
        {
            redisTemplate.delete(uuid);
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

}