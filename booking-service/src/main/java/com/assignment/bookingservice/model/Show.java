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
@Document(collection = "shows")
public class Show {
    @Id
    private String id;
    private String movieId;
    private String movieTitle;
    private String screenId;
    private String theatreId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
