
package tz.ac.dit.ipt_tracker.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AssessmentRequest(
        @NotNull(message = "Placement ID is required")
        Long placementId,

        @NotNull(message = "Visit date is required")
        LocalDate visitDate,

        @NotNull(message = "Score is required")
        @DecimalMin(value = "0.00", message = "Score cannot be below 0")
        @DecimalMax(value = "100.00", message = "Score cannot exceed 100")
        BigDecimal score,

        @Size(max = 10000, message = "Remarks are too long")
        String remarks
) {
}
