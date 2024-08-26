package com.practice.reservation.repository;

import com.practice.reservation.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation,Long> {

    Optional<Reservation> findByName(String name);
}
