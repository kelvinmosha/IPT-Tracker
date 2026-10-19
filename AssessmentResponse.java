
package tz.ac.dit.ipt_tracker.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record AssessmentResponse(
        Long id,
        Long placementId,
        Long assessorId,
        LocalDate visitDate,
        BigDecimal score,
        String remarks,
        LocalDateTime createdAt
) {
}
