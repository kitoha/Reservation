package com.practice.reservation.service

import com.practice.reservation.dto.ReservationDto
import com.practice.reservation.entity.Reservation
import com.practice.reservation.repository.ReservationRepository
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import spock.lang.Specification

import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@ExtendWith(SpringExtension.class)
@SpringBootTest
class RedisLockFacadeTest extends Specification {

    @Autowired
    ReservationService reservationService
    @Autowired
    ReservationRepository reservationRepository
    @Autowired
    RedisLockFacade redisLockFacade

    def cleanup() {
        reservationRepository.deleteAll()
    }

    /**
     * 동시성 제어 없이 예약을 하였을 때
     */
    def "Reservation non Concurrency Test"() {
        given:
        String testName = "nonLock"
        ReservationDto reservationDto = Mock()
        reservationDto.getReservedCount() >> 0
        reservationDto.getMaxCount() >> 25
        reservationDto.getName() >> testName
        reservationService.createReservation(reservationDto)
        int threadCount = 10
        ExecutorService executorService = Executors.newFixedThreadPool(10)
        CountDownLatch latch = new CountDownLatch(threadCount)

        when:
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    redisLockFacade.nonLock(testName)
                } catch (Exception e) {
                    throw new RuntimeException()
                } finally {
                    latch.countDown()
                }
            })
        }

        latch.await()
        executorService.shutdown();
        Reservation reservation = reservationRepository.findByName(testName).map { it -> it }.orElse(null)

        then:
        reservation.getReservedCount() < 10
    }

    /**
     * Redis SpinLock를 통한 동시성 제어를 하였을 때
     */
    def "Reservation Concurrency Test"() {
        given:
        String testName = "e"
        ReservationDto reservationDto = Mock()
        reservationDto.getReservedCount() >> 0
        reservationDto.getMaxCount() >> 25
        reservationDto.getName() >> testName
        reservationService.createReservation(reservationDto)
        int threadCount = 10
        ExecutorService executorService = Executors.newFixedThreadPool(10)
        CountDownLatch latch = new CountDownLatch(threadCount)

        when:
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    redisLockFacade.spinLock(testName)
                } catch (Exception e) {
                    throw new RuntimeException()
                } finally {
                    latch.countDown()
                }
            })
        }

        latch.await()
        executorService.shutdown();
        Reservation reservation = reservationRepository.findByName(testName).map { it -> it }.orElse(null)

        then:
        reservation.getReservedCount() == 10
    }

    /**
     * Redisson을 이용한 pub/sub의 메세지 브로커를 이용해 동시성 제어
     */
    def "Reservation Concurrency For Redisson Test"() {
        given:
        String testName = "tryLockTest"
        ReservationDto reservationDto = Mock()
        reservationDto.getReservedCount() >> 0
        reservationDto.getMaxCount() >> 25
        reservationDto.getName() >> testName
        reservationService.createReservation(reservationDto)
        int threadCount = 10
        ExecutorService executorService = Executors.newFixedThreadPool(10)
        CountDownLatch latch = new CountDownLatch(threadCount)

        when:
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    redisLockFacade.tryLock(testName)
                } catch (Exception e) {
                    throw new RuntimeException()
                } finally {
                    latch.countDown()
                }
            })
        }

        latch.await()
        executorService.shutdown();
        Reservation reservation = reservationRepository.findByName(testName).map { it -> it }.orElse(null)

        then:
        reservation.getReservedCount() == 10
    }

}
