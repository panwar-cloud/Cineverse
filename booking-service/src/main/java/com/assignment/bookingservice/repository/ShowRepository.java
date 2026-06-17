package com.assignment.bookingservice.repository;

import com.assignment.bookingservice.model.Show;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShowRepository extends MongoRepository<Show, String> {
    List<Show> findByMovieId(String movieId);
}
