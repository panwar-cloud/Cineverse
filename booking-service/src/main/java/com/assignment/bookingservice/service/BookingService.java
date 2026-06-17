package com.assignment.bookingservice.service;

import com.assignment.bookingservice.dto.ConfirmRequest;
import com.assignment.bookingservice.dto.LockRequest;
import com.assignment.bookingservice.exception.ResourceNotFoundException;
import com.assignment.bookingservice.exception.SeatAlreadyBookedException;
import com.assignment.bookingservice.model.Booking;
import com.assignment.bookingservice.model.Show;
import com.assignment.bookingservice.model.ShowSeat;
import com.assignment.bookingservice.repository.BookingRepository;
import com.assignment.bookingservice.repository.ShowRepository;
import com.assignment.bookingservice.repository.ShowSeatRepository;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ShowRepository showRepository;
    private final ShowSeatRepository showSeatRepository;
    private final MongoTemplate mongoTemplate;
    private final CacheManager cacheManager;

    public BookingService(
            BookingRepository bookingRepository,
            ShowRepository showRepository,
            ShowSeatRepository showSeatRepository,
            MongoTemplate mongoTemplate,
            CacheManager cacheManager
    ) {
        this.bookingRepository = bookingRepository;
        this.showRepository = showRepository;
        this.showSeatRepository = showSeatRepository;
        this.mongoTemplate = mongoTemplate;
        this.cacheManager = cacheManager;
    }

    @CacheEvict(value = "seats", key = "#request.showId")
    public Booking lockSeats(LockRequest request, String userEmail) {
        Show show = showRepository.findById(request.getShowId())
                .orElseThrow(() -> new ResourceNotFoundException("Show not found with id: " + request.getShowId()));

        List<String> lockedSeatIds = new ArrayList<>();
        double totalPrice = 0.0;

        try {
            for (String seatId : request.getSeatIds()) {
                String showSeatId = request.getShowId() + "_" + seatId;

                Query query = new Query(Criteria.where("_id").is(showSeatId)
                        .orOperator(
                                Criteria.where("status").is("AVAILABLE"),
                                Criteria.where("status").is("LOCKED").and("lockedUntil").lt(LocalDateTime.now()),
                                Criteria.where("status").is("LOCKED").and("userEmail").is(userEmail)
                        ));

                Update update = new Update()
                        .set("status", "LOCKED")
                        .set("lockedUntil", LocalDateTime.now().plusMinutes(5))
                        .set("userEmail", userEmail);

                ShowSeat showSeat = mongoTemplate.findAndModify(
                        query,
                        update,
                        FindAndModifyOptions.options().returnNew(true),
                        ShowSeat.class
                );

                if (showSeat == null) {
                    throw new SeatAlreadyBookedException("Seat " + seatId + " is currently locked or booked by another user.");
                }

                lockedSeatIds.add(seatId);
                totalPrice += showSeat.getPrice();
            }
        } catch (Exception ex) {
            for (String seatId : lockedSeatIds) {
                String showSeatId = request.getShowId() + "_" + seatId;
                Query rollbackQuery = new Query(Criteria.where("_id").is(showSeatId).and("userEmail").is(userEmail));
                Update rollbackUpdate = new Update()
                        .set("status", "AVAILABLE")
                        .set("lockedUntil", null)
                        .set("userEmail", null);
                mongoTemplate.updateFirst(rollbackQuery, rollbackUpdate, ShowSeat.class);
            }
            throw ex;
        }

        Booking booking = Booking.builder()
                .showId(request.getShowId())
                .movieTitle(show.getMovieTitle())
                .userEmail(userEmail)
                .seatIds(request.getSeatIds())
                .totalPrice(totalPrice)
                .status("INITIATED")
                .createdAt(LocalDateTime.now())
                .build();

        return bookingRepository.save(booking);
    }

    public Booking confirmBooking(ConfirmRequest request, String userEmail) {
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + request.getBookingId()));

        if (!booking.getUserEmail().equals(userEmail)) {
            throw new SeatAlreadyBookedException("Access denied: This booking does not belong to you.");
        }

        if (!"INITIATED".equals(booking.getStatus())) {
            throw new SeatAlreadyBookedException("Booking cannot be confirmed. Current status: " + booking.getStatus());
        }

        List<String> confirmedSeatIds = new ArrayList<>();
        try {
            for (String seatId : booking.getSeatIds()) {
                String showSeatId = booking.getShowId() + "_" + seatId;

                Query query = new Query(Criteria.where("_id").is(showSeatId)
                        .and("status").is("LOCKED")
                        .and("userEmail").is(userEmail));

                Update update = new Update()
                        .set("status", "BOOKED")
                        .set("lockedUntil", null);

                ShowSeat showSeat = mongoTemplate.findAndModify(
                        query,
                        update,
                        FindAndModifyOptions.options().returnNew(true),
                        ShowSeat.class
                );

                if (showSeat == null) {
                    throw new SeatAlreadyBookedException("Seat lock expired or invalidated for seat: " + seatId);
                }
                confirmedSeatIds.add(seatId);
            }
        } catch (Exception ex) {
            for (String seatId : confirmedSeatIds) {
                String showSeatId = booking.getShowId() + "_" + seatId;
                Query rollbackQuery = new Query(Criteria.where("_id").is(showSeatId).and("status").is("BOOKED"));
                Update rollbackUpdate = new Update()
                        .set("status", "LOCKED")
                        .set("lockedUntil", LocalDateTime.now().plusMinutes(5));
                mongoTemplate.updateFirst(rollbackQuery, rollbackUpdate, ShowSeat.class);
            }
            throw ex;
        }

        booking.setStatus("CONFIRMED");
        Booking saved = bookingRepository.save(booking);

        if (cacheManager.getCache("seats") != null) {
            cacheManager.getCache("seats").evict(saved.getShowId());
        }

        return saved;
    }

    public Booking cancelBooking(ConfirmRequest request, String userEmail) {
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + request.getBookingId()));

        if (!booking.getUserEmail().equals(userEmail)) {
            throw new SeatAlreadyBookedException("Access denied: This booking does not belong to you.");
        }

        if ("CANCELLED".equals(booking.getStatus())) {
            return booking;
        }

        for (String seatId : booking.getSeatIds()) {
            String showSeatId = booking.getShowId() + "_" + seatId;
            Query query = new Query(Criteria.where("_id").is(showSeatId));
            Update update = new Update()
                    .set("status", "AVAILABLE")
                    .set("lockedUntil", null)
                    .set("userEmail", null);
            mongoTemplate.updateFirst(query, update, ShowSeat.class);
        }

        booking.setStatus("CANCELLED");
        Booking saved = bookingRepository.save(booking);

        if (cacheManager.getCache("seats") != null) {
            cacheManager.getCache("seats").evict(saved.getShowId());
        }

        return saved;
    }

    @Cacheable(value = "seats", key = "#showId")
    public List<ShowSeat> getSeatsByShow(String showId) {
        showRepository.findById(showId)
                .orElseThrow(() -> new ResourceNotFoundException("Show not found with id: " + showId));
        return showSeatRepository.findByShowId(showId);
    }

    public List<Booking> getUserBookings(String userEmail) {
        return bookingRepository.findByUserEmail(userEmail);
    }
}
