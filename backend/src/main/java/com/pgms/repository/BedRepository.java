package com.pgms.repository;

import com.pgms.entity.Bed;
import com.pgms.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BedRepository extends JpaRepository<Bed, Long> {
    List<Bed> findByRoom(Room room);
}
