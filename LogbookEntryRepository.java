
package tz.ac.dit.ipt_tracker.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import tz.ac.dit.ipt_tracker.entity.LogbookEntry;
import tz.ac.dit.ipt_tracker.entity.LogbookStatus;

import java.time.LocalDate;
import java.util.List;

public interface LogbookEntryRepository
        extends JpaRepository<LogbookEntry, Long> {

    List<LogbookEntry> findByPlacementId(Long placementId);

    Page<LogbookEntry> findByPlacementId(
            Long placementId,
            Pageable pageable
    );

    List<LogbookEntry> findByPlacementStudentId(Long studentId);

    Page<LogbookEntry> findByPlacementStudentId(
            Long studentId,
            Pageable pageable
    );

    List<LogbookEntry> findByStatus(LogbookStatus status);

    boolean existsByPlacementIdAndEntryDate(
            Long placementId,
            LocalDate entryDate
    );
}
