package com.pgms.repository;

import com.pgms.entity.Property;
import com.pgms.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {
    List<Room> findByProperty(Property property);
}
