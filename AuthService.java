
package tz.ac.dit.ipt_tracker.service;

import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import tz.ac.dit.ipt_tracker.dto.AuthResponse;
import tz.ac.dit.ipt_tracker.dto.LoginRequest;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtEncoder jwtEncoder;
    private final long expirationSeconds;

    public AuthService(
            AuthenticationManager authenticationManager,
            JwtEncoder jwtEncoder,
            @Value("${ipt.jwt.expiration-seconds:3600}")
            long expirationSeconds) {

        this.authenticationManager = authenticationManager;
        this.jwtEncoder = jwtEncoder;
        this.expirationSeconds = expirationSeconds;
    }

    public AuthResponse login(LoginRequest request) {

        String email = request.email().trim().toLowerCase();

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                email,
                                request.password()
                        )
                );

        Instant now = Instant.now();

        List<String> authorities = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("ipt-tracker")
                .subject(authentication.getName())
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expirationSeconds))
                .claim("authorities", authorities)
                .build();

        JwsHeader header =
                JwsHeader.with(MacAlgorithm.HS256).build();

        String accessToken = jwtEncoder.encode(
                JwtEncoderParameters.from(header, claims)
        ).getTokenValue();

        return new AuthResponse(
                accessToken,
                "Bearer",
                expirationSeconds
        );
    }
}
