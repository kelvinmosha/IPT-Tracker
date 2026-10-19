
package tz.ac.dit.ipt_tracker.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import tz.ac.dit.ipt_tracker.entity.Placement;
import tz.ac.dit.ipt_tracker.entity.PlacementStatus;

import java.util.List;

public interface PlacementRepository
        extends JpaRepository<Placement, Long> {

    Page<Placement> findByStudentId(
            Long studentId,
            Pageable pageable
    );

    List<Placement> findByStudentId(Long studentId);

    Page<Placement> findByOrganisationId(
            Long organisationId,
            Pageable pageable
    );

    Page<Placement> findByIndustrialSupervisorId(
            Long supervisorId,
            Pageable pageable
    );

    List<Placement> findByIndustrialSupervisorId(Long supervisorId);

    Page<Placement> findByAcademicSupervisorId(
            Long supervisorId,
            Pageable pageable
    );

    List<Placement> findByAcademicSupervisorId(Long supervisorId);

    boolean existsByStudentIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Long studentId,
            java.time.LocalDate endDate,
            java.time.LocalDate startDate
    );

    Page<Placement> findByStatus(
            PlacementStatus status,
            Pageable pageable
    );
}
