package com.assignment.bookingservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "bookings")
public class Booking {
    @Id
    private String id;
    private String showId;
    private String movieTitle;
    private String userEmail;
    private List<String> seatIds;
    private Double totalPrice;
    private String status; // INITIATED, CONFIRMED, CANCELLED
    private LocalDateTime createdAt;
}
