package com.pgms.service;

import com.pgms.dto.request.LocationRequest;
import com.pgms.entity.Location;
import com.pgms.exception.ResourceNotFoundException;
import com.pgms.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final LocationRepository locationRepository;

    public Location create(LocationRequest request) {
        Location location = Location.builder()
                .city(request.getCity())
                .area(request.getArea())
                .pincode(request.getPincode())
                .active(true)
                .build();
        return locationRepository.save(location);
    }

    public List<Location> getAll() {
        return locationRepository.findAll();
    }

    public Location update(Long id, LocationRequest request) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found"));
        location.setCity(request.getCity());
        location.setArea(request.getArea());
        location.setPincode(request.getPincode());
        return locationRepository.save(location);
    }

    public void deactivate(Long id) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found"));
        location.setActive(false);
        locationRepository.save(location);
    }
}
