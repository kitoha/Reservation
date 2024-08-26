package com.practice.reservation.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RedisLockRepository {

    private final RedisTemplate<String,String> redisTemplate;

    public Boolean lock(String name){
        return redisTemplate.opsForValue().setIfAbsent(name, "lock", Duration.ofSeconds(1));
    }

    public Boolean unlock(String name){
        return redisTemplate.delete(name);
    }

}
