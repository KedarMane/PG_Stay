package com.pgms.service;

import com.pgms.dto.request.DiscountPlanRequest;
import com.pgms.entity.DiscountPlan;
import com.pgms.entity.Property;
import com.pgms.entity.User;
import com.pgms.exception.BadRequestException;
import com.pgms.exception.ResourceNotFoundException;
import com.pgms.repository.DiscountPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DiscountPlanService {

    private final DiscountPlanRepository discountPlanRepository;
    private final PropertyService propertyService;

    public DiscountPlan create(User owner, Long propertyId, DiscountPlanRequest request) {
        Property property = propertyService.getOwnedProperty(owner, propertyId);

        boolean duplicate = discountPlanRepository.findByProperty(property).stream()
                .anyMatch(p -> p.isActive() && p.getDurationMonths().equals(request.getDurationMonths()));
        if (duplicate) {
            throw new BadRequestException(
                    "There's already an active plan for " + request.getDurationMonths() + " months. Edit or deactivate it first.");
        }

        DiscountPlan plan = DiscountPlan.builder()
                .property(property)
                .durationMonths(request.getDurationMonths())
                .discountPercent(request.getDiscountPercent())
                .active(true)
                .build();
        return discountPlanRepository.save(plan);
    }

    public DiscountPlan update(User owner, Long propertyId, Long planId, DiscountPlanRequest request) {
        DiscountPlan plan = getOwnedPlan(owner, propertyId, planId);
        plan.setDurationMonths(request.getDurationMonths());
        plan.setDiscountPercent(request.getDiscountPercent());
        return discountPlanRepository.save(plan);
    }

    public DiscountPlan toggleActive(User owner, Long propertyId, Long planId) {
        DiscountPlan plan = getOwnedPlan(owner, propertyId, planId);
        plan.setActive(!plan.isActive());
        return discountPlanRepository.save(plan);
    }

    public void delete(User owner, Long propertyId, Long planId) {
        DiscountPlan plan = getOwnedPlan(owner, propertyId, planId);
        discountPlanRepository.delete(plan);
    }

    public List<DiscountPlan> getForOwner(User owner, Long propertyId) {
        Property property = propertyService.getOwnedProperty(owner, propertyId);
        return discountPlanRepository.findByProperty(property);
    }

    // Public: only active plans - what guests browsing an approved property get to see.
    public List<DiscountPlan> getActiveForProperty(Property property) {
        return discountPlanRepository.findByPropertyAndActiveTrue(property);
    }

    public DiscountPlan getActivePlanForDuration(Property property, Integer durationMonths) {
        return discountPlanRepository.findByPropertyAndDurationMonthsAndActiveTrue(property, durationMonths)
                .orElseThrow(() -> new BadRequestException(
                        "That stay-duration plan is no longer available for this property."));
    }

    private DiscountPlan getOwnedPlan(User owner, Long propertyId, Long planId) {
        Property property = propertyService.getOwnedProperty(owner, propertyId);
        DiscountPlan plan = discountPlanRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Discount plan not found"));
        if (!plan.getProperty().getId().equals(property.getId())) {
            throw new BadRequestException("This plan does not belong to that property");
        }
        return plan;
    }
}
