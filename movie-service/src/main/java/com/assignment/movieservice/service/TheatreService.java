package com.assignment.movieservice.service;

import com.assignment.movieservice.dto.RestPage;
import com.assignment.movieservice.exception.ResourceNotFoundException;
import com.assignment.movieservice.model.Theatre;
import com.assignment.movieservice.repository.LocationRepository;
import com.assignment.movieservice.repository.TheatreRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class TheatreService {

    private final TheatreRepository theatreRepository;
    private final LocationRepository locationRepository;

    public TheatreService(TheatreRepository theatreRepository, LocationRepository locationRepository) {
        this.theatreRepository = theatreRepository;
        this.locationRepository = locationRepository;
    }

    @CacheEvict(value = "theatres_list", allEntries = true)
    public Theatre createTheatre(Theatre theatre) {
        if (theatre.getLocationId() != null) {
            locationRepository.findById(theatre.getLocationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Location not found with id: " + theatre.getLocationId()));
        }
        return theatreRepository.save(theatre);
    }

    @Cacheable(value = "theatres_list", key = "{#locationId, #page, #size, #sortBy, #direction}")
    public RestPage<Theatre> getAllTheatres(String locationId, int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Theatre> result;
        if (locationId != null && !locationId.trim().isEmpty()) {
            result = theatreRepository.findByLocationId(locationId, pageable);
        } else {
            result = theatreRepository.findAll(pageable);
        }
        return new RestPage<>(result.getContent(), result.getNumber(), result.getSize(), result.getTotalElements());
    }

    @Cacheable(value = "theatres", key = "#id")
    public Theatre getTheatreById(String id) {
        return theatreRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Theatre not found with id: " + id));
    }

    @Caching(evict = {
            @CacheEvict(value = "theatres", key = "#id"),
            @CacheEvict(value = "theatres_list", allEntries = true)
    })
    public Theatre updateTheatre(String id, Theatre details) {
        Theatre theatre = theatreRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Theatre not found with id: " + id));

        if (details.getLocationId() != null) {
            locationRepository.findById(details.getLocationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Location not found with id: " + details.getLocationId()));
        }

        theatre.setName(details.getName());
        theatre.setLocationId(details.getLocationId());
        theatre.setAddress(details.getAddress());

        return theatreRepository.save(theatre);
    }

    @Caching(evict = {
            @CacheEvict(value = "theatres", key = "#id"),
            @CacheEvict(value = "theatres_list", allEntries = true)
    })
    public void deleteTheatre(String id) {
        Theatre theatre = theatreRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Theatre not found with id: " + id));
        theatreRepository.delete(theatre);
    }
}
