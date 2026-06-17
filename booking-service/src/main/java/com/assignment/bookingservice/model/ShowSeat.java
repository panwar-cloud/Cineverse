package com.assignment.bookingservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "show_seats")
public class ShowSeat {
    @Id
    private String id; // showId + "_" + seatId
    private String showId;
    private String seatId;
    private String rowName;
    private Integer seatNumber;
    private String type; // e.g., GOLD, SILVER, PLATINUM
    private Double price;
    private String status; // AVAILABLE, LOCKED, BOOKED
    private LocalDateTime lockedUntil;
    private String userEmail;
}
