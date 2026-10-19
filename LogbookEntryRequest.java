
package tz.ac.dit.ipt_tracker.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record LogbookEntryRequest(

    @NotNull(message = "Placement ID is required")
    Long placementId,

    @NotNull(message = "Entry date is required")
    @PastOrPresent(message = "Entry date cannot be in the future")
    LocalDate entryDate,

    @NotBlank(message = "Tasks done are required")
    @Size(max = 5000, message = "Tasks done cannot exceed 5000 characters")
    String tasksDone,

    @NotBlank(message = "Skills learned are required")
    @Size(max = 5000, message = "Skills learned cannot exceed 5000 characters")
    String skillsLearned,

    @NotNull(message = "Hours worked are required")
    @Min(value = 1, message = "Minimum hours worked is 1")
    @Max(value = 12, message = "Maximum hours worked is 12")
    Integer hoursWorked
) {
}
