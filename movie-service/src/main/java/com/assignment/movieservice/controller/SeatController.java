package com.assignment.movieservice.controller;

import com.assignment.movieservice.exception.ResourceNotFoundException;
import com.assignment.movieservice.model.Seat;
import com.assignment.movieservice.repository.ScreenRepository;
import com.assignment.movieservice.repository.SeatRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/seats")
public class SeatController {

    private final SeatRepository seatRepository;
    private final ScreenRepository screenRepository;

    public SeatController(SeatRepository seatRepository, ScreenRepository screenRepository) {
        this.seatRepository = seatRepository;
        this.screenRepository = screenRepository;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('THEATRE_OWNER', 'ADMIN')")
    public ResponseEntity<Seat> createSeat(@Valid @RequestBody Seat seat) {
        if (seat.getScreenId() != null) {
            screenRepository.findById(seat.getScreenId())
                    .orElseThrow(() -> new ResourceNotFoundException("Screen not found with id: " + seat.getScreenId()));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(seatRepository.save(seat));
    }

    @PostMapping("/bulk")
    @PreAuthorize("hasAnyRole('THEATRE_OWNER', 'ADMIN')")
    public ResponseEntity<List<Seat>> createSeatsBulk(@Valid @RequestBody List<Seat> seats) {
        for (Seat seat : seats) {
            if (seat.getScreenId() != null) {
                screenRepository.findById(seat.getScreenId())
                        .orElseThrow(() -> new ResourceNotFoundException("Screen not found with id: " + seat.getScreenId()));
            }
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(seatRepository.saveAll(seats));
    }

    @GetMapping("/screen/{screenId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Seat>> getSeatsByScreen(@PathVariable String screenId) {
        screenRepository.findById(screenId)
                .orElseThrow(() -> new ResourceNotFoundException("Screen not found with id: " + screenId));
        return ResponseEntity.ok(seatRepository.findByScreenId(screenId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Seat> getSeatById(@PathVariable String id) {
        Seat seat = seatRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Seat not found with id: " + id));
        return ResponseEntity.ok(seat);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('THEATRE_OWNER', 'ADMIN')")
    public ResponseEntity<Seat> updateSeat(@PathVariable String id, @Valid @RequestBody Seat details) {
        Seat seat = seatRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Seat not found with id: " + id));

        if (details.getScreenId() != null) {
            screenRepository.findById(details.getScreenId())
                    .orElseThrow(() -> new ResourceNotFoundException("Screen not found with id: " + details.getScreenId()));
        }

        seat.setScreenId(details.getScreenId());
        seat.setRowName(details.getRowName());
        seat.setSeatNumber(details.getSeatNumber());
        seat.setType(details.getType());
        seat.setPrice(details.getPrice());

        return ResponseEntity.ok(seatRepository.save(seat));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('THEATRE_OWNER', 'ADMIN')")
    public ResponseEntity<Void> deleteSeat(@PathVariable String id) {
        Seat seat = seatRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Seat not found with id: " + id));
        seatRepository.delete(seat);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/screen/{screenId}")
    @PreAuthorize("hasAnyRole('THEATRE_OWNER', 'ADMIN')")
    public ResponseEntity<Void> deleteSeatsByScreen(@PathVariable String screenId) {
        screenRepository.findById(screenId)
                .orElseThrow(() -> new ResourceNotFoundException("Screen not found with id: " + screenId));
        seatRepository.deleteByScreenId(screenId);
        return ResponseEntity.noContent().build();
    }
}
