package com.assignment.movieservice.controller;

import com.assignment.movieservice.exception.ResourceNotFoundException;
import com.assignment.movieservice.model.Screen;
import com.assignment.movieservice.repository.ScreenRepository;
import com.assignment.movieservice.repository.TheatreRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/screens")
public class ScreenController {

    private final ScreenRepository screenRepository;
    private final TheatreRepository theatreRepository;

    public ScreenController(ScreenRepository screenRepository, TheatreRepository theatreRepository) {
        this.screenRepository = screenRepository;
        this.theatreRepository = theatreRepository;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('THEATRE_OWNER', 'ADMIN')")
    public ResponseEntity<Screen> createScreen(@Valid @RequestBody Screen screen) {
        if (screen.getTheatreId() != null) {
            theatreRepository.findById(screen.getTheatreId())
                    .orElseThrow(() -> new ResourceNotFoundException("Theatre not found with id: " + screen.getTheatreId()));
        }
        Screen saved = screenRepository.save(screen);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Screen>> getAllScreens(@RequestParam(required = false) String theatreId) {
        if (theatreId != null && !theatreId.trim().isEmpty()) {
            return ResponseEntity.ok(screenRepository.findByTheatreId(theatreId));
        }
        return ResponseEntity.ok(screenRepository.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Screen> getScreenById(@PathVariable String id) {
        Screen screen = screenRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Screen not found with id: " + id));
        return ResponseEntity.ok(screen);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('THEATRE_OWNER', 'ADMIN')")
    public ResponseEntity<Screen> updateScreen(@PathVariable String id, @Valid @RequestBody Screen details) {
        Screen screen = screenRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Screen not found with id: " + id));

        if (details.getTheatreId() != null) {
            theatreRepository.findById(details.getTheatreId())
                    .orElseThrow(() -> new ResourceNotFoundException("Theatre not found with id: " + details.getTheatreId()));
        }

        screen.setName(details.getName());
        screen.setTheatreId(details.getTheatreId());
        screen.setTotalSeats(details.getTotalSeats());

        return ResponseEntity.ok(screenRepository.save(screen));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('THEATRE_OWNER', 'ADMIN')")
    public ResponseEntity<Void> deleteScreen(@PathVariable String id) {
        Screen screen = screenRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Screen not found with id: " + id));
        screenRepository.delete(screen);
        return ResponseEntity.noContent().build();
    }
}
