package com.pgms.service;

import com.pgms.dto.request.FacilityRequest;
import com.pgms.dto.request.PropertyRequest;
import com.pgms.entity.*;
import com.pgms.exception.BadRequestException;
import com.pgms.exception.ResourceNotFoundException;
import com.pgms.repository.FacilityRepository;
import com.pgms.repository.LocationRepository;
import com.pgms.repository.PropertyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PropertyService {

    private final PropertyRepository propertyRepository;
    private final LocationRepository locationRepository;
    private final FacilityRepository facilityRepository;

    public Property create(User owner, PropertyRequest request) {
        if (!owner.isProfileCompleted()) {
            throw new BadRequestException("Please complete your profile (govt ID etc.) before adding a property");
        }
        Location location = locationRepository.findById(request.getLocationId())
                .orElseThrow(() -> new ResourceNotFoundException("Selected location does not exist"));

        Property property = Property.builder()
                .owner(owner)
                .location(location)
                .name(request.getName())
                .description(request.getDescription())
                .address(request.getAddress())
                .genderPreference(request.getGenderPreference())
                .status(PropertyStatus.PENDING)
                .imageUrls(request.getImageUrls() == null ? List.of() : request.getImageUrls())
                .build();

        Property saved = propertyRepository.save(property);

        if (request.getFacilities() != null) {
            for (String facilityName : request.getFacilities()) {
                Facility facility = Facility.builder().property(saved).name(facilityName).build();
                saved.getFacilities().add(facility);
            }
            facilityRepository.saveAll(saved.getFacilities());
        }
        return saved;
    }

    // Owner edits a property. If it was REJECTED, resubmitting resets it to PENDING for re-review.
    public Property update(User owner, Long propertyId, PropertyRequest request) {
        Property property = getOwnedProperty(owner, propertyId);
        Location location = locationRepository.findById(request.getLocationId())
                .orElseThrow(() -> new ResourceNotFoundException("Selected location does not exist"));

        property.setName(request.getName());
        property.setDescription(request.getDescription());
        property.setAddress(request.getAddress());
        property.setGenderPreference(request.getGenderPreference());
        property.setLocation(location);
        if (request.getImageUrls() != null) property.setImageUrls(request.getImageUrls());

        if (property.getStatus() == PropertyStatus.REJECTED) {
            property.setStatus(PropertyStatus.PENDING);
            property.setRejectionReason(null);
        }
        return propertyRepository.save(property);
    }

    // Facilities can be edited any time post-approval without needing re-review.
    public Facility addFacility(User owner, Long propertyId, FacilityRequest request) {
        Property property = getOwnedProperty(owner, propertyId);
        Facility facility = Facility.builder()
                .property(property)
                .name(request.getName())
                .icon(request.getIcon())
                .build();
        return facilityRepository.save(facility);
    }

    public void removeFacility(User owner, Long propertyId, Long facilityId) {
        Property property = getOwnedProperty(owner, propertyId);
        Facility facility = facilityRepository.findById(facilityId)
                .orElseThrow(() -> new ResourceNotFoundException("Facility not found"));
        if (!facility.getProperty().getId().equals(property.getId())) {
            throw new BadRequestException("Facility does not belong to this property");
        }
        facilityRepository.delete(facility);
    }

    public List<Property> getMyProperties(User owner) {
        return propertyRepository.findByOwner(owner);
    }

    public List<Property> getPending() {
        return propertyRepository.findByStatus(PropertyStatus.PENDING);
    }

    public List<Property> getAll() {
        return propertyRepository.findAll();
    }

    public Property approve(Long propertyId) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found"));
        property.setStatus(PropertyStatus.APPROVED);
        property.setApprovedAt(LocalDateTime.now());
        property.setRejectionReason(null);
        return propertyRepository.save(property);
    }

    public Property reject(Long propertyId, String reason) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found"));
        property.setStatus(PropertyStatus.REJECTED);
        property.setRejectionReason(reason);
        return propertyRepository.save(property);
    }

    public List<Property> searchApproved(String name, String location) {
        if (name != null && !name.isBlank()) {
            return propertyRepository.findByStatusAndNameContainingIgnoreCase(PropertyStatus.APPROVED, name);
        }
        if (location != null && !location.isBlank()) {
            return propertyRepository
                    .findByStatusAndLocation_CityContainingIgnoreCaseOrStatusAndLocation_AreaContainingIgnoreCase(
                            PropertyStatus.APPROVED, location, PropertyStatus.APPROVED, location);
        }
        return propertyRepository.findByStatus(PropertyStatus.APPROVED);
    }

    public Property getApprovedById(Long id) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found"));
        if (property.getStatus() != PropertyStatus.APPROVED) {
            throw new ResourceNotFoundException("Property not found");
        }
        return property;
    }

    public Property getOwnedProperty(User owner, Long propertyId) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found"));
        if (!property.getOwner().getId().equals(owner.getId())) {
            throw new BadRequestException("This property does not belong to you");
        }
        return property;
    }
}
