package com.assignment.movieservice.service;

import com.assignment.movieservice.dto.RestPage;
import com.assignment.movieservice.exception.ResourceNotFoundException;
import com.assignment.movieservice.model.Movie;
import com.assignment.movieservice.repository.MovieRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class MovieService {

    private final MovieRepository movieRepository;

    public MovieService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    @CacheEvict(value = {"movies_list", "movies_search"}, allEntries = true)
    public Movie createMovie(Movie movie) {
        return movieRepository.save(movie);
    }

    @Cacheable(value = "movies_list", key = "{#page, #size, #sortBy, #direction}")
    public RestPage<Movie> getAllMovies(int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Movie> result = movieRepository.findAll(pageable);
        return new RestPage<>(result.getContent(), result.getNumber(), result.getSize(), result.getTotalElements());
    }

    @Cacheable(value = "movies_search", key = "{#title, #genre, #page, #size, #sortBy, #direction}")
    public RestPage<Movie> searchMovies(String title, String genre, int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Movie> result;
        if (title != null && genre != null) {
            result = movieRepository.findByTitleContainingIgnoreCaseAndGenreContainingIgnoreCase(title, genre, pageable);
        } else if (title != null) {
            result = movieRepository.findByTitleContainingIgnoreCase(title, pageable);
        } else if (genre != null) {
            result = movieRepository.findByGenreContainingIgnoreCase(genre, pageable);
        } else {
            result = movieRepository.findAll(pageable);
        }
        return new RestPage<>(result.getContent(), result.getNumber(), result.getSize(), result.getTotalElements());
    }

    @Cacheable(value = "movies", key = "#id")
    public Movie getMovieById(String id) {
        return movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + id));
    }

    @Caching(evict = {
            @CacheEvict(value = "movies", key = "#id"),
            @CacheEvict(value = {"movies_list", "movies_search"}, allEntries = true)
    })
    public Movie updateMovie(String id, Movie details) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + id));

        movie.setTitle(details.getTitle());
        movie.setGenre(details.getGenre());
        movie.setDuration(details.getDuration());
        movie.setRating(details.getRating());
        movie.setReleaseDate(details.getReleaseDate());

        return movieRepository.save(movie);
    }

    @Caching(evict = {
            @CacheEvict(value = "movies", key = "#id"),
            @CacheEvict(value = {"movies_list", "movies_search"}, allEntries = true)
    })
    public void deleteMovie(String id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + id));
        movieRepository.delete(movie);
    }
}
