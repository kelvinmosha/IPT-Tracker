
package tz.ac.dit.ipt_tracker.dto;

public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresIn
) {
}
