
package tz.ac.dit.ipt_tracker.controller;

import jakarta.validation.Valid;

import java.security.Principal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import tz.ac.dit.ipt_tracker.dto.AssessmentRequest;
import tz.ac.dit.ipt_tracker.dto.AssessmentResponse;
import tz.ac.dit.ipt_tracker.service.AssessmentService;

@RestController
@RequestMapping("/api/v1/assessments")
public class AssessmentController {

    private final AssessmentService service;

    public AssessmentController(AssessmentService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','ACADEMIC_SUPERVISOR')")
    public Page<AssessmentResponse> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return service.findAll(PageRequest.of(
                page,
                Math.min(Math.max(size, 1), 50),
                Sort.by("id").descending()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','ACADEMIC_SUPERVISOR')")
    public AssessmentResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @GetMapping("/placement/{placementId}")
    @PreAuthorize("hasAnyRole('ADMIN','ACADEMIC_SUPERVISOR')")
    public AssessmentResponse findByPlacement(
            @PathVariable Long placementId) {
        return service.findByPlacementId(placementId);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','ACADEMIC_SUPERVISOR')")
    @ResponseStatus(HttpStatus.CREATED)
    public AssessmentResponse create(
            @Valid @RequestBody AssessmentRequest request,
            Principal principal) {
        return service.create(request, principal.getName());
    }
}
