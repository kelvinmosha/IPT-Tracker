
package tz.ac.dit.ipt_tracker.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OrganisationRequest(
        @NotBlank(message = "Organisation name is required")
        @Size(max = 150)
        String name,

        @Size(max = 100)
        String sector,

        @Size(max = 100)
        String region,

        @Size(max = 255)
        String address,

        @Email(message = "Contact email must be valid")
        @Size(max = 150)
        String contactEmail
) {
}
