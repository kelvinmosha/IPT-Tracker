
package tz.ac.dit.ipt_tracker.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Full name is required")
        @Size(max = 150)
        String fullName,

        @Size(max = 50)
        String registrationNumber,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        @Size(max = 150)
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 72)
        String password
) {
}
