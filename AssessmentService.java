
package tz.ac.dit.ipt_tracker.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tz.ac.dit.ipt_tracker.dto.AssessmentRequest;
import tz.ac.dit.ipt_tracker.dto.AssessmentResponse;
import tz.ac.dit.ipt_tracker.entity.Assessment;
import tz.ac.dit.ipt_tracker.entity.Placement;
import tz.ac.dit.ipt_tracker.entity.Role;
import tz.ac.dit.ipt_tracker.entity.User;
import tz.ac.dit.ipt_tracker.exception.DuplicateResourceException;
import tz.ac.dit.ipt_tracker.exception.ResourceNotFoundException;
import tz.ac.dit.ipt_tracker.repository.AssessmentRepository;
import tz.ac.dit.ipt_tracker.repository.PlacementRepository;
import tz.ac.dit.ipt_tracker.repository.UserRepository;

@Service
@Transactional
public class AssessmentService {

    private final AssessmentRepository assessmentRepository;
    private final PlacementRepository placementRepository;
    private final UserRepository userRepository;

    public AssessmentService(
            AssessmentRepository assessmentRepository,
            PlacementRepository placementRepository,
            UserRepository userRepository) {
        this.assessmentRepository = assessmentRepository;
        this.placementRepository = placementRepository;
        this.userRepository = userRepository;
    }

    public AssessmentResponse create(
            AssessmentRequest request,
            String authenticatedEmail) {

        User assessor = getUserByEmail(authenticatedEmail);

        if (assessor.getRole() != Role.ACADEMIC_SUPERVISOR
                && assessor.getRole() != Role.ADMIN) {
            throw new IllegalArgumentException(
                    "Only an academic supervisor or ADMIN can create assessments");
        }

        Placement placement = getPlacement(request.placementId());

        if (assessor.getRole() == Role.ACADEMIC_SUPERVISOR
                && (placement.getAcademicSupervisor() == null
                || !placement.getAcademicSupervisor().getId()
                        .equals(assessor.getId()))) {
            throw new IllegalArgumentException(
                    "You are not the academic supervisor assigned to this placement");
        }

        if (assessmentRepository.existsByPlacementId(placement.getId())) {
            throw new DuplicateResourceException(
                    "An assessment already exists for this placement");
        }

        validateScore(request.score());

        Assessment assessment = new Assessment();
        assessment.setPlacement(placement);
        assessment.setAssessor(assessor);
        assessment.setVisitDate(request.visitDate());
        assessment.setScore(request.score());
        assessment.setRemarks(request.remarks());

        return toResponse(assessmentRepository.save(assessment));
    }

    @Transactional(readOnly = true)
    public List<AssessmentResponse> findAll() {
        return assessmentRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<AssessmentResponse> findAll(Pageable pageable) {
        return assessmentRepository.findAll(pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public AssessmentResponse findById(Long id) {
        Assessment assessment = assessmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Assessment not found with ID: " + id));

        return toResponse(assessment);
    }

    @Transactional(readOnly = true)
    public AssessmentResponse findByPlacementId(Long placementId) {
        return assessmentRepository.findByPlacementId(placementId)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Assessment not found for placement ID: " + placementId));
    }

    private Placement getPlacement(Long id) {
        return placementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Placement not found with ID: " + id));
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Authenticated user was not found"));
    }

    private void validateScore(BigDecimal score) {
        if (score == null
                || score.compareTo(BigDecimal.ZERO) < 0
                || score.compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException(
                    "Assessment score must be between 0 and 100");
        }
    }

    private AssessmentResponse toResponse(Assessment assessment) {
        return new AssessmentResponse(
                assessment.getId(),
                assessment.getPlacement().getId(),
                assessment.getAssessor().getId(),
                assessment.getVisitDate(),
                assessment.getScore(),
                assessment.getRemarks(),
                assessment.getCreatedAt());
    }
}
