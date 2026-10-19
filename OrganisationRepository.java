
package tz.ac.dit.ipt_tracker.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import tz.ac.dit.ipt_tracker.entity.Organisation;

public interface OrganisationRepository
        extends JpaRepository<Organisation, Long> {

    Optional<Organisation> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    Page<Organisation> findByRegionIgnoreCase(
            String region,
            Pageable pageable
    );

    Page<Organisation> findBySectorIgnoreCase(
            String sector,
            Pageable pageable
    );
}
