package com.assignment.movieservice.repository;

import com.assignment.movieservice.model.Seat;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeatRepository extends MongoRepository<Seat, String> {
    List<Seat> findByScreenId(String screenId);
    void deleteByScreenId(String screenId);
}
