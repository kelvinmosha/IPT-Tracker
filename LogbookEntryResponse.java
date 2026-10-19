
package tz.ac.dit.ipt_tracker.dto;

import tz.ac.dit.ipt_tracker.entity.LogbookStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record LogbookEntryResponse(
    Long id,
    Long placementId,
    Long studentId,
    LocalDate entryDate,
    String tasksDone,
    String skillsLearned,
    Integer hoursWorked,
    LogbookStatus status,
    String supervisorComment,
    LocalDateTime submittedAt,
    LocalDateTime reviewedAt,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
