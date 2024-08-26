package com.practice.reservation.dto;

import lombok.Getter;

@Getter
public class ReservationDto {
    private String name;
    private int reservedCount;
    private int maxCount;
}
