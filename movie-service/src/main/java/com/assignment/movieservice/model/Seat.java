package com.assignment.movieservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "seats")
public class Seat {
    @Id
    private String id;
    private String screenId; // Link to Screen
    private String rowName; // e.g., A, B, C
    private Integer seatNumber; // e.g., 1, 2, 3
    private String type; // e.g., GOLD, SILVER, PLATINUM
    private Double price;
}
