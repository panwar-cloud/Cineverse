package com.assignment.bookingservice;

import com.assignment.bookingservice.dto.ConfirmRequest;
import com.assignment.bookingservice.dto.LockRequest;
import com.assignment.bookingservice.model.Booking;
import com.assignment.bookingservice.model.Show;
import com.assignment.bookingservice.model.ShowSeat;
import com.assignment.bookingservice.repository.BookingRepository;
import com.assignment.bookingservice.repository.ShowRepository;
import com.assignment.bookingservice.repository.ShowSeatRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BookingServiceApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ShowRepository showRepository;

    @Autowired
    private ShowSeatRepository showSeatRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private org.springframework.cache.CacheManager cacheManager;

    private String showId;

    @BeforeEach
    void setupData() {
        bookingRepository.deleteAll();
        showSeatRepository.deleteAll();
        showRepository.deleteAll();
        if (cacheManager != null) {
            cacheManager.getCacheNames().forEach(name -> cacheManager.getCache(name).clear());
        }

        Show show = Show.builder()
                .movieId("movie-123")
                .movieTitle("Inception")
                .screenId("screen-456")
                .theatreId("theatre-789")
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(2))
                .build();
        Show savedShow = showRepository.save(show);
        this.showId = savedShow.getId();

        ShowSeat seat1 = ShowSeat.builder()
                .id(showId + "_seat-A1")
                .showId(showId)
                .seatId("seat-A1")
                .rowName("A")
                .seatNumber(1)
                .type("GOLD")
                .price(250.0)
                .status("AVAILABLE")
                .build();

        ShowSeat seat2 = ShowSeat.builder()
                .id(showId + "_seat-A2")
                .showId(showId)
                .seatId("seat-A2")
                .rowName("A")
                .seatNumber(2)
                .type("GOLD")
                .price(250.0)
                .status("AVAILABLE")
                .build();

        showSeatRepository.saveAll(Arrays.asList(seat1, seat2));
    }

    @Test
    void testLockAndConfirmBookingSuccess() throws Exception {
        LockRequest lockRequest = new LockRequest(showId, Collections.singletonList("seat-A1"));

        MvcResult lockResult = mockMvc.perform(post("/booking/lock")
                        .header("X-User-Email", "user@test.com")
                        .header("X-User-Role", "USER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(lockRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("INITIATED")))
                .andExpect(jsonPath("$.totalPrice", is(250.0)))
                .andReturn();

        Booking booking = objectMapper.readValue(lockResult.getResponse().getContentAsString(), Booking.class);

        ConfirmRequest confirmRequest = new ConfirmRequest(booking.getId());
        mockMvc.perform(post("/booking/confirm")
                        .header("X-User-Email", "user@test.com")
                        .header("X-User-Role", "USER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(confirmRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CONFIRMED")));
    }

    @Test
    void testLockAndCancelBookingSuccess() throws Exception {
        LockRequest lockRequest = new LockRequest(showId, Collections.singletonList("seat-A2"));

        MvcResult lockResult = mockMvc.perform(post("/booking/lock")
                        .header("X-User-Email", "user@test.com")
                        .header("X-User-Role", "USER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(lockRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        Booking booking = objectMapper.readValue(lockResult.getResponse().getContentAsString(), Booking.class);

        ConfirmRequest cancelRequest = new ConfirmRequest(booking.getId());
        mockMvc.perform(post("/booking/cancel")
                        .header("X-User-Email", "user@test.com")
                        .header("X-User-Role", "USER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cancelRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CANCELLED")));

        mockMvc.perform(get("/booking/show/" + showId + "/seats")
                        .header("X-User-Email", "user@test.com")
                        .header("X-User-Role", "USER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[1].status", is("AVAILABLE")));
    }

    @Test
    void testConcurrentDoubleBookingPrevention() throws Exception {
        int numberOfThreads = 8;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(1);

        LockRequest lockRequest = new LockRequest(showId, Collections.singletonList("seat-A1"));
        String payload = objectMapper.writeValueAsString(lockRequest);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        List<Callable<Void>> tasks = new ArrayList<>();
        for (int i = 0; i < numberOfThreads; i++) {
            final String email = "user" + i + "@test.com";
            tasks.add(() -> {
                try {
                    latch.await();
                    MvcResult result = mockMvc.perform(post("/booking/lock")
                                    .header("X-User-Email", email)
                                    .header("X-User-Role", "USER")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(payload))
                            .andReturn();

                    int status = result.getResponse().getStatus();
                    if (status == 201) {
                        successCount.incrementAndGet();
                    } else if (status == 400) {
                        failureCount.incrementAndGet();
                    }
                } catch (Exception ignored) {
                }
                return null;
            });
        }

        List<Future<Void>> futures = new ArrayList<>();
        for (Callable<Void> task : tasks) {
            futures.add(executorService.submit(task));
        }

        latch.countDown();
        executorService.shutdown();
        executorService.awaitTermination(5, TimeUnit.SECONDS);

        assertEquals(1, successCount.get(), "Exactly one user should succeed in booking the seat");
        assertEquals(numberOfThreads - 1, failureCount.get(), "All other concurrent users should be rejected");
    }

    @Test
    void testShowAndSeatCachingAndEviction() throws Exception {
        // Query seats (populates cache)
        mockMvc.perform(get("/booking/show/" + showId + "/seats")
                        .header("X-User-Email", "user@test.com")
                        .header("X-User-Role", "USER"))
                .andExpect(status().isOk());

        org.springframework.cache.Cache seatsCache = cacheManager.getCache("seats");
        org.junit.jupiter.api.Assertions.assertNotNull(seatsCache);
        org.junit.jupiter.api.Assertions.assertNotNull(seatsCache.get(showId));

        // Perform lock (evicts cache)
        LockRequest lockRequest = new LockRequest(showId, Collections.singletonList("seat-A1"));
        mockMvc.perform(post("/booking/lock")
                        .header("X-User-Email", "user@test.com")
                        .header("X-User-Role", "USER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(lockRequest)))
                .andExpect(status().isCreated());

        org.junit.jupiter.api.Assertions.assertNull(seatsCache.get(showId));
    }
}
