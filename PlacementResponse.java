
package tz.ac.dit.ipt_tracker.dto;

import tz.ac.dit.ipt_tracker.entity.PlacementStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PlacementResponse(
        Long id,
        Long studentId,
        String studentName,
        Long organisationId,
        String organisationName,
        Long industrialSupervisorId,
        Long academicSupervisorId,
        String department,
        LocalDate startDate,
        LocalDate endDate,
        PlacementStatus status,
        LocalDateTime createdAt
) {
}
