
package tz.ac.dit.ipt_tracker.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tz.ac.dit.ipt_tracker.dto.LogbookEntryRequest;
import tz.ac.dit.ipt_tracker.dto.LogbookEntryResponse;
import tz.ac.dit.ipt_tracker.entity.LogbookEntry;
import tz.ac.dit.ipt_tracker.entity.LogbookStatus;
import tz.ac.dit.ipt_tracker.entity.Placement;
import tz.ac.dit.ipt_tracker.entity.Role;
import tz.ac.dit.ipt_tracker.entity.User;
import tz.ac.dit.ipt_tracker.exception.DuplicateResourceException;
import tz.ac.dit.ipt_tracker.exception.ResourceNotFoundException;
import tz.ac.dit.ipt_tracker.repository.LogbookEntryRepository;
import tz.ac.dit.ipt_tracker.repository.PlacementRepository;
import tz.ac.dit.ipt_tracker.repository.UserRepository;

@Service
@Transactional
public class LogbookEntryService {

    private final LogbookEntryRepository entryRepository;
    private final PlacementRepository placementRepository;
    private final UserRepository userRepository;

    public LogbookEntryService(
            LogbookEntryRepository entryRepository,
            PlacementRepository placementRepository,
            UserRepository userRepository) {
        this.entryRepository = entryRepository;
        this.placementRepository = placementRepository;
        this.userRepository = userRepository;
    }

    public LogbookEntryResponse create(
            LogbookEntryRequest request,
            String authenticatedEmail) {

        User student = getUserByEmail(authenticatedEmail);

        if (student.getRole() != Role.STUDENT) {
            throw new IllegalArgumentException(
                    "Only students can create logbook entries");
        }

        Placement placement = getPlacement(request.placementId());
        verifyStudentOwnsPlacement(student, placement);
        validateEntry(request, placement);

        if (entryRepository.existsByPlacementIdAndEntryDate(
                placement.getId(), request.entryDate())) {
            throw new DuplicateResourceException(
                    "A logbook entry already exists for this placement and date");
        }

        LogbookEntry entry = new LogbookEntry();
        entry.setPlacement(placement);
        entry.setEntryDate(request.entryDate());
        entry.setTasksDone(request.tasksDone().trim());
        entry.setSkillsLearned(request.skillsLearned().trim());
        entry.setHoursWorked(request.hoursWorked());
        entry.setStatus(LogbookStatus.DRAFT);

        return toResponse(entryRepository.save(entry));
    }

    public LogbookEntryResponse update(
            Long id,
            LogbookEntryRequest request,
            String authenticatedEmail) {

        User student = getUserByEmail(authenticatedEmail);
        LogbookEntry entry = getEntry(id);

        verifyStudentOwnsPlacement(student, entry.getPlacement());

        if (!entry.isEditable()) {
            throw new IllegalStateException(
                    "Only DRAFT or REJECTED entries can be edited");
        }

        Placement newPlacement = getPlacement(request.placementId());
        verifyStudentOwnsPlacement(student, newPlacement);
        validateEntry(request, newPlacement);

        boolean placementOrDateChanged =
                !entry.getPlacement().getId().equals(newPlacement.getId())
                || !entry.getEntryDate().equals(request.entryDate());

        if (placementOrDateChanged
                && entryRepository.existsByPlacementIdAndEntryDate(
                        newPlacement.getId(), request.entryDate())) {
            throw new DuplicateResourceException(
                    "A logbook entry already exists for this placement and date");
        }

        entry.setPlacement(newPlacement);
        entry.setEntryDate(request.entryDate());
        entry.setTasksDone(request.tasksDone().trim());
        entry.setSkillsLearned(request.skillsLearned().trim());
        entry.setHoursWorked(request.hoursWorked());

        return toResponse(entryRepository.save(entry));
    }

    public LogbookEntryResponse submit(
            Long id,
            String authenticatedEmail) {

        User student = getUserByEmail(authenticatedEmail);
        LogbookEntry entry = getEntry(id);

        verifyStudentOwnsPlacement(student, entry.getPlacement());

        if (!entry.isEditable()) {
            throw new IllegalStateException(
                    "Only DRAFT or REJECTED entries can be submitted");
        }

        entry.setStatus(LogbookStatus.SUBMITTED);
        entry.setSubmittedAt(LocalDateTime.now());
        entry.setReviewedAt(null);
        entry.setSupervisorComment(null);

        return toResponse(entryRepository.save(entry));
    }

    public LogbookEntryResponse review(
            Long id,
            boolean approved,
            String comment,
            String authenticatedEmail) {

        User supervisor = getUserByEmail(authenticatedEmail);
        LogbookEntry entry = getEntry(id);

        if (supervisor.getRole() != Role.INDUSTRIAL_SUPERVISOR) {
            throw new IllegalArgumentException(
                    "Only an industrial supervisor can review logbook entries");
        }

        User assignedSupervisor =
                entry.getPlacement().getIndustrialSupervisor();

        if (assignedSupervisor == null
                || !assignedSupervisor.getId().equals(supervisor.getId())) {
            throw new IllegalArgumentException(
                    "You are not the industrial supervisor assigned to this placement");
        }

        if (entry.getStatus() != LogbookStatus.SUBMITTED) {
            throw new IllegalStateException(
                    "Only SUBMITTED entries can be reviewed");
        }

        if (!approved && (comment == null || comment.isBlank())) {
            throw new IllegalArgumentException(
                    "A comment is required when rejecting an entry");
        }

        entry.setStatus(
                approved ? LogbookStatus.APPROVED : LogbookStatus.REJECTED);
        entry.setSupervisorComment(
                comment == null || comment.isBlank() ? null : comment.trim());
        entry.setReviewedAt(LocalDateTime.now());

        return toResponse(entryRepository.save(entry));
    }

    public void deleteDraft(Long id, String authenticatedEmail) {
        User student = getUserByEmail(authenticatedEmail);
        LogbookEntry entry = getEntry(id);

        verifyStudentOwnsPlacement(student, entry.getPlacement());

        if (entry.getStatus() != LogbookStatus.DRAFT) {
            throw new IllegalStateException(
                    "Only DRAFT entries can be deleted");
        }

        entryRepository.delete(entry);
    }

    @Transactional(readOnly = true)
    public LogbookEntryResponse findById(
            Long id,
            String authenticatedEmail) {

        User user = getUserByEmail(authenticatedEmail);
        LogbookEntry entry = getEntry(id);

        if (!canViewEntry(user, entry)) {
            throw new IllegalArgumentException(
                    "You are not allowed to view this logbook entry");
        }

        return toResponse(entry);
    }

    @Transactional(readOnly = true)
    public List<LogbookEntryResponse> findByPlacement(
            Long placementId,
            String authenticatedEmail) {

        User user = getUserByEmail(authenticatedEmail);
        Placement placement = getPlacement(placementId);

        if (!canViewPlacement(user, placement)) {
            throw new IllegalArgumentException(
                    "You are not allowed to view this placement's logbook");
        }

        return entryRepository.findByPlacementId(placementId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LogbookEntryResponse> findAllForUser(
            String authenticatedEmail) {

        User user = getUserByEmail(authenticatedEmail);

        List<LogbookEntry> entries;

        switch (user.getRole()) {
            case ADMIN ->
                entries = entryRepository.findAll();

            case STUDENT ->
                entries = entryRepository.findByPlacementStudentId(user.getId());

            case INDUSTRIAL_SUPERVISOR -> {
                List<Placement> placements =
                        placementRepository.findByIndustrialSupervisorId(user.getId());

                entries = placements.stream()
                        .flatMap(p -> entryRepository.findByPlacementId(p.getId())
                                .stream())
                        .toList();
            }

            case ACADEMIC_SUPERVISOR -> {
                List<Placement> placements =
                        placementRepository.findByAcademicSupervisorId(user.getId());

                entries = placements.stream()
                        .flatMap(p -> entryRepository.findByPlacementId(p.getId())
                                .stream())
                        .toList();
            }

            default -> throw new IllegalArgumentException("Unsupported role");
        }

        return entries.stream().map(this::toResponse).toList();
    }

    private boolean canViewEntry(User user, LogbookEntry entry) {
        return canViewPlacement(user, entry.getPlacement());
    }

    private boolean canViewPlacement(User user, Placement placement) {
        if (user.getRole() == Role.ADMIN) {
            return true;
        }

        if (user.getRole() == Role.STUDENT) {
            return placement.getStudent().getId().equals(user.getId());
        }

        if (user.getRole() == Role.INDUSTRIAL_SUPERVISOR) {
            return placement.getIndustrialSupervisor() != null
                    && placement.getIndustrialSupervisor().getId()
                            .equals(user.getId());
        }

        if (user.getRole() == Role.ACADEMIC_SUPERVISOR) {
            return placement.getAcademicSupervisor() != null
                    && placement.getAcademicSupervisor().getId()
                            .equals(user.getId());
        }

        return false;
    }

    private void verifyStudentOwnsPlacement(
            User student, Placement placement) {

        if (student.getRole() != Role.STUDENT
                || !placement.getStudent().getId().equals(student.getId())) {
            throw new IllegalArgumentException(
                    "You can only manage logbook entries for your own placement");
        }
    }

    private void validateEntry(
            LogbookEntryRequest request, Placement placement) {

        LocalDate date = request.entryDate();

        if (date == null
                || date.isBefore(placement.getStartDate())
                || date.isAfter(placement.getEndDate())) {
            throw new IllegalArgumentException(
                    "Entry date must be within the placement period");
        }

        if (date.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "Entry date cannot be in the future");
        }

        if (request.hoursWorked() == null
                || request.hoursWorked() < 1
                || request.hoursWorked() > 12) {
            throw new IllegalArgumentException(
                    "Hours worked must be between 1 and 12");
        }

        if (request.tasksDone() == null || request.tasksDone().isBlank()
                || request.skillsLearned() == null
                || request.skillsLearned().isBlank()) {
            throw new IllegalArgumentException(
                    "Tasks done and skills learned are required");
        }
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found for email: " + email));
    }

    private Placement getPlacement(Long id) {
        return placementRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Placement not found with ID: " + id));
    }

    private LogbookEntry getEntry(Long id) {
        return entryRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Logbook entry not found with ID: " + id));
    }

    private LogbookEntryResponse toResponse(LogbookEntry entry) {
        return new LogbookEntryResponse(
                entry.getId(),
                entry.getPlacement().getId(),
                entry.getPlacement().getStudent().getId(),
                entry.getEntryDate(),
                entry.getTasksDone(),
                entry.getSkillsLearned(),
                entry.getHoursWorked(),
                entry.getStatus(),
                entry.getSupervisorComment(),
                entry.getSubmittedAt(),
                entry.getReviewedAt(),
                entry.getCreatedAt(),
                entry.getUpdatedAt()
        );
    }
}
