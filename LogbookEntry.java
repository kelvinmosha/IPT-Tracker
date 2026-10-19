
package tz.ac.dit.ipt_tracker.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "logbook_entries",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_logbook_placement_date",
            columnNames = {"placement_id", "entry_date"}
        )
    }
)
@Getter
@Setter
public class LogbookEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "placement_id", nullable = false)
    private Placement placement;

    @Column(name = "entry_date", nullable = false)
    private LocalDate entryDate;

    @Column(name = "tasks_done", nullable = false, columnDefinition = "TEXT")
    private String tasksDone;

    @Column(name = "skills_learned", nullable = false, columnDefinition = "TEXT")
    private String skillsLearned;

    @Column(name = "hours_worked", nullable = false)
    private Integer hoursWorked;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LogbookStatus status = LogbookStatus.DRAFT;

    @Column(name = "supervisor_comment", columnDefinition = "TEXT")
    private String supervisorComment;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public boolean isEditable() {
        return status == LogbookStatus.DRAFT
                || status == LogbookStatus.REJECTED;
    }
}
