package com.assignment.movieservice.repository;

import com.assignment.movieservice.model.Theatre;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TheatreRepository extends MongoRepository<Theatre, String> {
    Page<Theatre> findByLocationId(String locationId, Pageable pageable);
    List<Theatre> findByLocationId(String locationId);
}
