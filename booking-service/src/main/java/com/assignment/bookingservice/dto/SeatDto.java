package com.assignment.bookingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatDto {
    private String id;
    private String screenId;
    private String rowName;
    private Integer seatNumber;
    private String type;
    private Double price;
}
