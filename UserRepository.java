
package tz.ac.dit.ipt_tracker.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import tz.ac.dit.ipt_tracker.entity.Role;
import tz.ac.dit.ipt_tracker.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByRegistrationNumberIgnoreCase(String registrationNumber);

    Page<User> findByRole(Role role, Pageable pageable);

    Page<User> findByActiveTrue(Pageable pageable);
}
