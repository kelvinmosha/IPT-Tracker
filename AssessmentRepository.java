
package tz.ac.dit.ipt_tracker.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import tz.ac.dit.ipt_tracker.entity.Assessment;

public interface AssessmentRepository
        extends JpaRepository<Assessment, Long> {

    Optional<Assessment> findByPlacementId(Long placementId);

    List<Assessment> findByAssessorId(Long assessorId);

    Page<Assessment> findByAssessorId(
            Long assessorId,
            Pageable pageable
    );

    boolean existsByPlacementId(Long placementId);
}
