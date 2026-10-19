
package tz.ac.dit.ipt_tracker.controller;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import tz.ac.dit.ipt_tracker.dto.AuthResponse;
import tz.ac.dit.ipt_tracker.dto.LoginRequest;
import tz.ac.dit.ipt_tracker.dto.RegisterRequest;
import tz.ac.dit.ipt_tracker.dto.UserResponse;
import tz.ac.dit.ipt_tracker.service.AuthService;
import tz.ac.dit.ipt_tracker.service.UserService;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    public AuthController(
            AuthService authService,
            UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(
            @Valid @RequestBody RegisterRequest request) {
        return userService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(
            @Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
