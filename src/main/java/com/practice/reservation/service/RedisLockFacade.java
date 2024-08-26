package com.practice.reservation.service;

import com.practice.reservation.repository.RedisLockRepository;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisLockFacade {
    private final RedisLockRepository redisLockRepository;
    private final ReservationService reservationService;
    private final RedissonClient redissonClient;

    public boolean nonLock(String name){
        return reservationService.requestReservation(name);
    }

    public boolean spinLock(String name) throws InterruptedException{
        while(!redisLockRepository.lock(name)){
            Thread.sleep(100L);
        }

        return reservationService.requestReservation(name);
    }

    public boolean tryLock(String name){
        RLock lock = redissonClient.getLock(name);

        try{
            boolean available = lock.tryLock(100L,1, TimeUnit.SECONDS);

            if(!available){
                return false;
            }
            reservationService.requestReservation(name);
        }catch (Exception e){
            throw new RuntimeException(e);
        }finally {
            lock.unlock();
        }

        return true;
    }



}
