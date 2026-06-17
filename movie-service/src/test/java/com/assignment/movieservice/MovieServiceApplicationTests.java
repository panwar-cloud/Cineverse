package com.assignment.movieservice;

import com.assignment.movieservice.model.Location;
import com.assignment.movieservice.model.Movie;
import com.assignment.movieservice.repository.LocationRepository;
import com.assignment.movieservice.repository.MovieRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class MovieServiceApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private org.springframework.cache.CacheManager cacheManager;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void cleanDatabase() {
        movieRepository.deleteAll();
        locationRepository.deleteAll();
        if (cacheManager != null) {
            cacheManager.getCacheNames().forEach(name -> cacheManager.getCache(name).clear());
        }
    }

    @Test
    void contextLoads() {
        // Basic context loading check
    }

    @Test
    void testUserViewMoviesSuccess() throws Exception {
        Movie m1 = Movie.builder().title("Inception").genre("Sci-Fi").duration(148).rating(8.8).releaseDate(LocalDate.of(2010, 7, 16)).build();
        Movie m2 = Movie.builder().title("The Dark Knight").genre("Action").duration(152).rating(9.0).releaseDate(LocalDate.of(2008, 7, 18)).build();
        movieRepository.save(m1);
        movieRepository.save(m2);

        mockMvc.perform(get("/movies")
                        .header("X-User-Email", "user@example.com")
                        .header("X-User-Role", "USER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].title", anyOf(is("Inception"), is("The Dark Knight"))));
    }

    @Test
    void testTheatreOwnerManageMoviesSuccess() throws Exception {
        Movie movie = Movie.builder()
                .title("Interstellar")
                .genre("Sci-Fi")
                .duration(169)
                .rating(8.6)
                .releaseDate(LocalDate.of(2014, 11, 7))
                .build();

        mockMvc.perform(post("/movies")
                        .header("X-User-Email", "owner@example.com")
                        .header("X-User-Role", "THEATRE_OWNER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(movie)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is("Interstellar")));
    }

    @Test
    void testUserManageMoviesForbidden() throws Exception {
        Movie movie = Movie.builder()
                .title("Interstellar")
                .genre("Sci-Fi")
                .duration(169)
                .rating(8.6)
                .releaseDate(LocalDate.of(2014, 11, 7))
                .build();

        mockMvc.perform(post("/movies")
                        .header("X-User-Email", "user@example.com")
                        .header("X-User-Role", "USER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(movie)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error", is("Forbidden")));
    }

    @Test
    void testAdminManageLocationsSuccess() throws Exception {
        Location location = Location.builder()
                .name("IMAX Nexus")
                .city("Bangalore")
                .state("Karnataka")
                .build();

        mockMvc.perform(post("/locations")
                        .header("X-User-Email", "admin@example.com")
                        .header("X-User-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(location)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("IMAX Nexus")));
    }

    @Test
    void testTheatreOwnerManageLocationsForbidden() throws Exception {
        Location location = Location.builder()
                .name("IMAX Nexus")
                .city("Bangalore")
                .state("Karnataka")
                .build();

        mockMvc.perform(post("/locations")
                        .header("X-User-Email", "owner@example.com")
                        .header("X-User-Role", "THEATRE_OWNER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(location)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error", is("Forbidden")));
    }

    @Test
    void testMovieCachingAndEviction() throws Exception {
        Movie movie = Movie.builder()
                .title("CacheTestMovie")
                .genre("Sci-Fi")
                .duration(120)
                .rating(8.0)
                .releaseDate(LocalDate.of(2026, 1, 1))
                .build();
        Movie saved = movieRepository.save(movie);

        mockMvc.perform(get("/movies/" + saved.getId())
                        .header("X-User-Email", "user@example.com")
                        .header("X-User-Role", "USER"))
                .andExpect(status().isOk());

        org.springframework.cache.Cache moviesCache = cacheManager.getCache("movies");
        org.junit.jupiter.api.Assertions.assertNotNull(moviesCache);
        org.junit.jupiter.api.Assertions.assertNotNull(moviesCache.get(saved.getId()));

        Movie details = Movie.builder()
                .title("CacheTestMovieUpdated")
                .genre("Sci-Fi")
                .duration(120)
                .rating(8.0)
                .releaseDate(LocalDate.of(2026, 1, 1))
                .build();

        mockMvc.perform(put("/movies/" + saved.getId())
                        .header("X-User-Email", "admin@example.com")
                        .header("X-User-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(details)))
                .andExpect(status().isOk());

        org.junit.jupiter.api.Assertions.assertNull(moviesCache.get(saved.getId()));
    }
}
