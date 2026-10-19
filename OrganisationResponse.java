
package tz.ac.dit.ipt_tracker.dto;

import java.time.LocalDateTime;

public record OrganisationResponse(
        Long id,
        String name,
        String sector,
        String region,
        String address,
        String contactEmail,
        LocalDateTime createdAt
) {
}
