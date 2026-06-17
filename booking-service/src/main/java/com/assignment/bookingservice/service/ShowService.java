package com.assignment.bookingservice.service;

import com.assignment.bookingservice.dto.MovieDto;
import com.assignment.bookingservice.dto.ScreenDto;
import com.assignment.bookingservice.dto.SeatDto;
import com.assignment.bookingservice.dto.ShowRequest;
import com.assignment.bookingservice.exception.ResourceNotFoundException;
import com.assignment.bookingservice.model.Show;
import com.assignment.bookingservice.model.ShowSeat;
import com.assignment.bookingservice.repository.ShowRepository;
import com.assignment.bookingservice.repository.ShowSeatRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class ShowService {

    private final ShowRepository showRepository;
    private final ShowSeatRepository showSeatRepository;
    private final RestTemplate restTemplate;
    private final String movieServiceUrl;

    public ShowService(
            ShowRepository showRepository,
            ShowSeatRepository showSeatRepository,
            RestTemplate restTemplate,
            @Value("${application.movie-service.url}") String movieServiceUrl
    ) {
        this.showRepository = showRepository;
        this.showSeatRepository = showSeatRepository;
        this.restTemplate = restTemplate;
        this.movieServiceUrl = movieServiceUrl;
    }

    @CacheEvict(value = "shows_list", allEntries = true)
    public Show createShow(ShowRequest request, HttpServletRequest servletRequest) {
        String userEmail = servletRequest.getHeader("X-User-Email");
        String userRole = servletRequest.getHeader("X-User-Role");

        HttpHeaders headers = new HttpHeaders();
        if (userEmail != null) headers.set("X-User-Email", userEmail);
        if (userRole != null) headers.set("X-User-Role", userRole);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        MovieDto movie;
        try {
            ResponseEntity<MovieDto> movieRes = restTemplate.exchange(
                    movieServiceUrl + "/" + request.getMovieId(),
                    HttpMethod.GET,
                    entity,
                    MovieDto.class
            );
            movie = movieRes.getBody();
        } catch (HttpClientErrorException.NotFound ex) {
            throw new ResourceNotFoundException("Movie not found with id: " + request.getMovieId());
        }

        ScreenDto screen;
        try {
            String screenUrl = movieServiceUrl.replace("/movies", "/screens") + "/" + request.getScreenId();
            ResponseEntity<ScreenDto> screenRes = restTemplate.exchange(
                    screenUrl,
                    HttpMethod.GET,
                    entity,
                    ScreenDto.class
            );
            screen = screenRes.getBody();
        } catch (HttpClientErrorException.NotFound ex) {
            throw new ResourceNotFoundException("Screen not found with id: " + request.getScreenId());
        }

        SeatDto[] seats;
        try {
            String seatsUrl = movieServiceUrl.replace("/movies", "/seats") + "/screen/" + request.getScreenId();
            ResponseEntity<SeatDto[]> seatsRes = restTemplate.exchange(
                    seatsUrl,
                    HttpMethod.GET,
                    entity,
                    SeatDto[].class
            );
            seats = seatsRes.getBody();
        } catch (Exception ex) {
            seats = new SeatDto[0];
        }

        Show show = Show.builder()
                .movieId(request.getMovieId())
                .movieTitle(movie != null ? movie.getTitle() : "Unknown Movie")
                .screenId(request.getScreenId())
                .theatreId(screen != null ? screen.getTheatreId() : "Unknown Theatre")
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .build();
        Show savedShow = showRepository.save(show);

        List<ShowSeat> showSeats = new ArrayList<>();
        if (seats != null) {
            for (SeatDto seat : seats) {
                ShowSeat showSeat = ShowSeat.builder()
                        .id(savedShow.getId() + "_" + seat.getId())
                        .showId(savedShow.getId())
                        .seatId(seat.getId())
                        .rowName(seat.getRowName())
                        .seatNumber(seat.getSeatNumber())
                        .type(seat.getType())
                        .price(seat.getPrice())
                        .status("AVAILABLE")
                        .build();
                showSeats.add(showSeat);
            }
            showSeatRepository.saveAll(showSeats);
        }

        return savedShow;
    }

    @Cacheable(value = "shows_list")
    public List<Show> getAllShows() {
        return showRepository.findAll();
    }

    @Cacheable(value = "shows", key = "#id")
    public Show getShowById(String id) {
        return showRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Show not found with id: " + id));
    }
}
