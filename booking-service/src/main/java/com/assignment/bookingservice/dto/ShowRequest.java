package com.assignment.bookingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShowRequest {
    private String movieId;
    private String screenId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
