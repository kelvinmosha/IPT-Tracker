
package tz.ac.dit.ipt_tracker.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import tz.ac.dit.ipt_tracker.dto.PlacementRequest;
import tz.ac.dit.ipt_tracker.dto.PlacementResponse;
import tz.ac.dit.ipt_tracker.entity.Organisation;
import tz.ac.dit.ipt_tracker.entity.Placement;
import tz.ac.dit.ipt_tracker.entity.PlacementStatus;
import tz.ac.dit.ipt_tracker.entity.Role;
import tz.ac.dit.ipt_tracker.entity.User;
import tz.ac.dit.ipt_tracker.repository.OrganisationRepository;
import tz.ac.dit.ipt_tracker.repository.PlacementRepository;
import tz.ac.dit.ipt_tracker.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class PlacementService {

    private final PlacementRepository placementRepository;
    private final UserRepository userRepository;
    private final OrganisationRepository organisationRepository;

    public PlacementResponse create(PlacementRequest request) {
        validateDates(request.startDate(), request.endDate());

        User student = getUserWithRole(request.studentId(), Role.STUDENT);
        Organisation organisation = getOrganisation(request.organisationId());

        User industrialSupervisor = request.industrialSupervisorId() == null
                ? null
                : getUserWithRole(
                        request.industrialSupervisorId(),
                        Role.INDUSTRIAL_SUPERVISOR);

        User academicSupervisor = request.academicSupervisorId() == null
                ? null
                : getUserWithRole(
                        request.academicSupervisorId(),
                        Role.ACADEMIC_SUPERVISOR);

        validateNoOverlap(
                student.getId(),
                request.startDate(),
                request.endDate(),
                null);

        Placement placement = new Placement();
        placement.setStudent(student);
        placement.setOrganisation(organisation);
        placement.setIndustrialSupervisor(industrialSupervisor);
        placement.setAcademicSupervisor(academicSupervisor);
        placement.setDepartment(request.department().trim());
        placement.setStartDate(request.startDate());
        placement.setEndDate(request.endDate());
        placement.setStatus(PlacementStatus.ACTIVE);

        return toResponse(placementRepository.save(placement));
    }

    public PlacementResponse update(Long id, PlacementRequest request) {
        validateDates(request.startDate(), request.endDate());

        Placement placement = getPlacement(id);
        User student = getUserWithRole(request.studentId(), Role.STUDENT);
        Organisation organisation = getOrganisation(request.organisationId());

        User industrialSupervisor = request.industrialSupervisorId() == null
                ? null
                : getUserWithRole(
                        request.industrialSupervisorId(),
                        Role.INDUSTRIAL_SUPERVISOR);

        User academicSupervisor = request.academicSupervisorId() == null
                ? null
                : getUserWithRole(
                        request.academicSupervisorId(),
                        Role.ACADEMIC_SUPERVISOR);

        validateNoOverlap(
                student.getId(),
                request.startDate(),
                request.endDate(),
                placement.getId());

        placement.setStudent(student);
        placement.setOrganisation(organisation);
        placement.setIndustrialSupervisor(industrialSupervisor);
        placement.setAcademicSupervisor(academicSupervisor);
        placement.setDepartment(request.department().trim());
        placement.setStartDate(request.startDate());
        placement.setEndDate(request.endDate());

        return toResponse(placementRepository.save(placement));
    }

    @Transactional(readOnly = true)
    public List<PlacementResponse> findAllForUser(String email) {
        User user = getUserByEmail(email);

        List<Placement> placements = switch (user.getRole()) {
            case ADMIN -> placementRepository.findAll();
            case STUDENT -> placementRepository.findByStudentId(user.getId());
            case INDUSTRIAL_SUPERVISOR ->
                    placementRepository.findByIndustrialSupervisorId(user.getId());
            case ACADEMIC_SUPERVISOR ->
                    placementRepository.findByAcademicSupervisorId(user.getId());
        };

        return placements.stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PlacementResponse findByIdForUser(Long id, String email) {
        User user = getUserByEmail(email);
        Placement placement = getPlacement(id);

        if (!canViewPlacement(placement, user)) {
            throw new IllegalStateException(
                    "You are not allowed to view this placement.");
        }

        return toResponse(placement);
    }

    public void delete(Long id) {
        Placement placement = getPlacement(id);
        placementRepository.delete(placement);
    }

    private void validateDates(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException(
                    "Placement start date and end date are required.");
        }

        if (!startDate.isBefore(endDate)) {
            throw new IllegalArgumentException(
                    "Placement start date must be before end date.");
        }
    }

    private void validateNoOverlap(
            Long studentId,
            LocalDate newStart,
            LocalDate newEnd,
            Long ignoredPlacementId) {

        List<Placement> existingPlacements =
                placementRepository.findByStudentId(studentId);

        boolean overlaps = existingPlacements.stream()
                .filter(p -> ignoredPlacementId == null
                        || !p.getId().equals(ignoredPlacementId))
                .anyMatch(p ->
                        !newEnd.isBefore(p.getStartDate())
                        && !newStart.isAfter(p.getEndDate()));

        if (overlaps) {
            throw new IllegalArgumentException(
                    "This student already has a placement with overlapping dates.");
        }
    }

    private boolean canViewPlacement(Placement placement, User user) {
        return switch (user.getRole()) {
            case ADMIN -> true;
            case STUDENT ->
                    placement.getStudent().getId().equals(user.getId());
            case INDUSTRIAL_SUPERVISOR ->
                    placement.getIndustrialSupervisor() != null
                    && placement.getIndustrialSupervisor()
                            .getId().equals(user.getId());
            case ACADEMIC_SUPERVISOR ->
                    placement.getAcademicSupervisor() != null
                    && placement.getAcademicSupervisor()
                            .getId().equals(user.getId());
        };
    }

    private User getUserWithRole(Long id, Role requiredRole) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User not found with ID: " + id));

        if (user.getRole() != requiredRole) {
            throw new IllegalArgumentException(
                    "User ID " + id + " must have role " + requiredRole + ".");
        }

        if (!user.isActive()) {
            throw new IllegalArgumentException(
                    "The selected user account is inactive.");
        }

        return user;
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Authenticated user was not found."));
    }

    private Organisation getOrganisation(Long id) {
        return organisationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Organisation not found with ID: " + id));
    }

    private Placement getPlacement(Long id) {
        return placementRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Placement not found with ID: " + id));
    }

    private PlacementResponse toResponse(Placement placement) {
        return new PlacementResponse(
                placement.getId(),
                placement.getStudent().getId(),
                placement.getStudent().getFullName(),
                placement.getOrganisation().getId(),
                placement.getOrganisation().getName(),
                placement.getIndustrialSupervisor() == null
                        ? null
                        : placement.getIndustrialSupervisor().getId(),
                placement.getAcademicSupervisor() == null
                        ? null
                        : placement.getAcademicSupervisor().getId(),
                placement.getDepartment(),
                placement.getStartDate(),
                placement.getEndDate(),
                placement.getStatus(),
                placement.getCreatedAt());
    }
}
