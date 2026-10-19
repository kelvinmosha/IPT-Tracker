
package tz.ac.dit.ipt_tracker.dto;

import tz.ac.dit.ipt_tracker.entity.Role;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String fullName,
        String registrationNumber,
        String email,
        Role role,
        String phone,
        boolean active,
        LocalDateTime createdAt
) {
}
