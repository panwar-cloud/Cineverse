package com.assignment.movieservice.repository;

import com.assignment.movieservice.model.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MovieRepository extends MongoRepository<Movie, String> {
    Page<Movie> findByTitleContainingIgnoreCaseAndGenreContainingIgnoreCase(String title, String genre, Pageable pageable);
    Page<Movie> findByTitleContainingIgnoreCase(String title, Pageable pageable);
    Page<Movie> findByGenreContainingIgnoreCase(String genre, Pageable pageable);
}
