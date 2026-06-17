package com.assignment.bookingservice.controller;

import com.assignment.bookingservice.dto.ConfirmRequest;
import com.assignment.bookingservice.dto.LockRequest;
import com.assignment.bookingservice.model.Booking;
import com.assignment.bookingservice.model.ShowSeat;
import com.assignment.bookingservice.service.BookingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/booking")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    private String getCurrentUserEmail() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return principal != null ? principal.toString() : "anonymous@example.com";
    }

    @PostMapping("/lock")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Booking> lockSeats(@RequestBody LockRequest request) {
        String userEmail = getCurrentUserEmail();
        Booking booking = bookingService.lockSeats(request, userEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(booking);
    }

    @PostMapping("/confirm")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Booking> confirmBooking(@RequestBody ConfirmRequest request) {
        String userEmail = getCurrentUserEmail();
        Booking booking = bookingService.confirmBooking(request, userEmail);
        return ResponseEntity.ok(booking);
    }

    @PostMapping("/cancel")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Booking> cancelBooking(@RequestBody ConfirmRequest request) {
        String userEmail = getCurrentUserEmail();
        Booking booking = bookingService.cancelBooking(request, userEmail);
        return ResponseEntity.ok(booking);
    }

    @GetMapping("/show/{showId}/seats")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ShowSeat>> getSeatsByShow(@PathVariable String showId) {
        return ResponseEntity.ok(bookingService.getSeatsByShow(showId));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Booking>> getUserBookings() {
        String userEmail = getCurrentUserEmail();
        return ResponseEntity.ok(bookingService.getUserBookings(userEmail));
    }
}
