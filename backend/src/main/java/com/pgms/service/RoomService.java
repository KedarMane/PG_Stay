package com.pgms.service;

import com.pgms.dto.request.RoomRequest;
import com.pgms.entity.*;
import com.pgms.exception.BadRequestException;
import com.pgms.exception.ResourceNotFoundException;
import com.pgms.repository.BedRepository;
import com.pgms.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final BedRepository bedRepository;
    private final PropertyService propertyService;

    public Room create(User owner, Long propertyId, RoomRequest request) {
        Property property = propertyService.getOwnedProperty(owner, propertyId);
        if (property.getStatus() != PropertyStatus.APPROVED) {
            throw new BadRequestException("Rooms can only be added once the property is approved by admin");
        }

        RoomType type;
        try {
            type = RoomType.valueOf(request.getType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Room type must be PRIVATE or SHARED");
        }

        Room room = Room.builder()
                .property(property)
                .type(type)
                .roomNumber(request.getRoomNumber())
                .description(request.getDescription())
                .monthlyRent(request.getMonthlyRent())
                .build();
        Room savedRoom = roomRepository.save(room);

        if (type == RoomType.PRIVATE) {
            if (request.getMonthlyRent() == null) {
                throw new BadRequestException("monthlyRent is required for a PRIVATE room");
            }
            Bed bed = Bed.builder()
                    .room(savedRoom)
                    .label("Private")
                    .monthlyRent(request.getMonthlyRent())
                    .status(BedStatus.AVAILABLE)
                    .build();
            bedRepository.save(bed);
        } else {
            if (request.getBeds() == null || request.getBeds().isEmpty()) {
                throw new BadRequestException("At least one bed is required for a SHARED room");
            }
            for (RoomRequest.BedInput bedInput : request.getBeds()) {
                BigDecimal rent = bedInput.getMonthlyRent() != null ? bedInput.getMonthlyRent() : request.getMonthlyRent();
                if (rent == null) {
                    throw new BadRequestException("monthlyRent is required for each bed");
                }
                Bed bed = Bed.builder()
                        .room(savedRoom)
                        .label(bedInput.getLabel())
                        .monthlyRent(rent)
                        .status(BedStatus.AVAILABLE)
                        .build();
                bedRepository.save(bed);
            }
        }
        return savedRoom;
    }

    public List<Room> getRoomsForProperty(Property property) {
        return roomRepository.findByProperty(property);
    }

    public Room getById(Long id) {
        return roomRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Room not found"));
    }
}
