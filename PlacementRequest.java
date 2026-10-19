
package tz.ac.dit.ipt_tracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record PlacementRequest(
        @NotNull(message = "Student ID is required")
        Long studentId,

        @NotNull(message = "Organisation ID is required")
        Long organisationId,

        Long industrialSupervisorId,

        Long academicSupervisorId,

        @NotBlank(message = "Department is required")
        @Size(max = 100)
        String department,

        @NotNull(message = "Start date is required")
        LocalDate startDate,

        @NotNull(message = "End date is required")
        LocalDate endDate
) {
    @AssertTrue(message = "Start date must be before end date")
    public boolean isDateRangeValid() {
        return startDate == null
                || endDate == null
                || startDate.isBefore(endDate);
    }
}
