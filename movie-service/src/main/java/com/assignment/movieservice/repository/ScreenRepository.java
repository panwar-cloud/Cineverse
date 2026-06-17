package com.assignment.movieservice.repository;

import com.assignment.movieservice.model.Screen;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScreenRepository extends MongoRepository<Screen, String> {
    List<Screen> findByTheatreId(String theatreId);
}
