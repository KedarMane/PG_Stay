package com.pgms.repository;

import com.pgms.entity.Property;
import com.pgms.entity.PropertyStatus;
import com.pgms.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PropertyRepository extends JpaRepository<Property, Long> {
    List<Property> findByStatus(PropertyStatus status);
    List<Property> findByOwner(User owner);
    List<Property> findByStatusAndNameContainingIgnoreCase(PropertyStatus status, String name);
    List<Property> findByStatusAndLocation_CityContainingIgnoreCaseOrStatusAndLocation_AreaContainingIgnoreCase(
            PropertyStatus status1, String city, PropertyStatus status2, String area);
}
