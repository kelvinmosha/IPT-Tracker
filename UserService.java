
package tz.ac.dit.ipt_tracker.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tz.ac.dit.ipt_tracker.dto.RegisterRequest;
import tz.ac.dit.ipt_tracker.dto.UserResponse;
import tz.ac.dit.ipt_tracker.entity.Role;
import tz.ac.dit.ipt_tracker.entity.User;
import tz.ac.dit.ipt_tracker.exception.DuplicateResourceException;
import tz.ac.dit.ipt_tracker.exception.ResourceNotFoundException;
import tz.ac.dit.ipt_tracker.repository.UserRepository;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponse register(RegisterRequest request) {

        String email = request.email().trim().toLowerCase();

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateResourceException(
                    "Email is already registered"
            );
        }

        String registrationNumber = request.registrationNumber();

        if (registrationNumber != null
                && !registrationNumber.isBlank()) {

            registrationNumber = registrationNumber.trim();

            if (userRepository.existsByRegistrationNumberIgnoreCase(
                    registrationNumber)) {

                throw new DuplicateResourceException(
                        "Registration number is already registered"
                );
            }
        } else {
            registrationNumber = null;
        }

        User user = new User();

        user.setFullName(request.fullName().trim());
        user.setRegistrationNumber(registrationNumber);
        user.setEmail(email);
        user.setPasswordHash(
                passwordEncoder.encode(request.password())
        );
        user.setRole(Role.STUDENT);
        user.setActive(true);

        User savedUser = userRepository.save(user);

        return toResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {

        return userRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> findAll(Pageable pageable) {

        return userRepository.findAll(pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with ID: " + id
                        )
                );

        return toResponse(user);
    }

    private UserResponse toResponse(User user) {

        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getRegistrationNumber(),
                user.getEmail(),
                user.getRole(),
                user.getPhone(),
                user.isActive(),
                user.getCreatedAt()
        );
    }
}
