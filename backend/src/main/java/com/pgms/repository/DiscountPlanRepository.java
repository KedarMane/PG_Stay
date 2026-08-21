package com.pgms.repository;

import com.pgms.entity.DiscountPlan;
import com.pgms.entity.Property;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DiscountPlanRepository extends JpaRepository<DiscountPlan, Long> {
    List<DiscountPlan> findByProperty(Property property);
    List<DiscountPlan> findByPropertyAndActiveTrue(Property property);
    Optional<DiscountPlan> findByPropertyAndDurationMonthsAndActiveTrue(Property property, Integer durationMonths);
}
