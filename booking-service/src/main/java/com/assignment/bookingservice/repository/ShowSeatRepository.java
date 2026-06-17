package com.assignment.bookingservice.repository;

import com.assignment.bookingservice.model.ShowSeat;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShowSeatRepository extends MongoRepository<ShowSeat, String> {
    List<ShowSeat> findByShowId(String showId);
}
