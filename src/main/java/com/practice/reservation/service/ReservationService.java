package com.practice.reservation.service;

import com.practice.reservation.repository.RedisLockRepository;
import com.practice.reservation.dto.ReservationDto;
import com.practice.reservation.entity.Reservation;
import com.practice.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final RedisLockRepository redisLockRepository;
    private final ReservationRepository reservationRepository;

    public boolean createReservation(ReservationDto reservationDto){

        Optional<Reservation> reservation = reservationRepository.findByName(reservationDto.getName());

        if(reservation.isPresent()){
            return false;
        }

        Reservation newReservation = Reservation.builder()
                .id(null)
                .name(reservationDto.getName())
                .reservedCount(reservationDto.getReservedCount())
                .maxCount(reservationDto.getMaxCount())
                .build();

        reservationRepository.save(newReservation);

        return true;
    }

    @Transactional
    public boolean requestReservation(String name) {

        Reservation reservation = reservationRepository.findByName(name).orElseThrow(()-> new RuntimeException("not Found Reservation"));

        if(isMaxReservation(reservation)) {
            return false;
        }

        Reservation newReservation = Reservation.builder()
                .id(reservation.getId())
                .name(reservation.getName())
                .reservedCount(reservation.getReservedCount()+1)
                .maxCount(reservation.getMaxCount())
                .build();

        reservationRepository.save(newReservation);

        return true;
    }

    private boolean isMaxReservation(Reservation reservation){
        return reservation.getReservedCount()>=reservation.getMaxCount();
    }
}
