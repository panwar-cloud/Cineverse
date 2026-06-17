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
@Document(collection = "screens")
public class Screen {
    @Id
    private String id;
    private String name;
    private String theatreId; // Link to Theatre
    private Integer totalSeats;
}
