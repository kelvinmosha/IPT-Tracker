
package tz.ac.dit.ipt_tracker.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import tz.ac.dit.ipt_tracker.dto.LogbookEntryRequest;
import tz.ac.dit.ipt_tracker.dto.LogbookEntryResponse;
import tz.ac.dit.ipt_tracker.service.LogbookEntryService;

@RestController
@RequestMapping("/api/v1")
public class LogbookEntryController {

    private final LogbookEntryService logbookEntryService;

    public LogbookEntryController(
            LogbookEntryService logbookEntryService) {
        this.logbookEntryService = logbookEntryService;
    }

    @GetMapping("/logbook-entries")
    @PreAuthorize("isAuthenticated()")
    public List<LogbookEntryResponse> findAll(
            @AuthenticationPrincipal Jwt jwt) {
        return logbookEntryService.findAllForUser(jwt.getSubject());
    }

    @GetMapping("/logbook-entries/{id}")
    @PreAuthorize("isAuthenticated()")
    public LogbookEntryResponse findById(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        return logbookEntryService.findById(id, jwt.getSubject());
    }

    @GetMapping("/placements/{placementId}/entries")
    @PreAuthorize("isAuthenticated()")
    public List<LogbookEntryResponse> findByPlacement(
            @PathVariable Long placementId,
            @AuthenticationPrincipal Jwt jwt) {
        return logbookEntryService.findByPlacement(
                placementId, jwt.getSubject());
    }

    @PostMapping("/logbook-entries")
    @PreAuthorize("hasRole('STUDENT')")
    @ResponseStatus(HttpStatus.CREATED)
    public LogbookEntryResponse create(
            @Valid @RequestBody LogbookEntryRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return logbookEntryService.create(request, jwt.getSubject());
    }

    @PutMapping("/logbook-entries/{id}")
    @PreAuthorize("hasRole('STUDENT')")
    public LogbookEntryResponse update(
            @PathVariable Long id,
            @Valid @RequestBody LogbookEntryRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return logbookEntryService.update(
                id, request, jwt.getSubject());
    }

    @PostMapping("/logbook-entries/{id}/submit")
    @PreAuthorize("hasRole('STUDENT')")
    public LogbookEntryResponse submit(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        return logbookEntryService.submit(id, jwt.getSubject());
    }

    @PostMapping("/logbook-entries/{id}/review")
    @PreAuthorize("hasRole('INDUSTRIAL_SUPERVISOR')")
    public LogbookEntryResponse review(
            @PathVariable Long id,
            @RequestParam boolean approved,
            @RequestParam(required = false) String comment,
            @AuthenticationPrincipal Jwt jwt) {
        return logbookEntryService.review(
                id, approved, comment, jwt.getSubject());
    }

    @DeleteMapping("/logbook-entries/{id}")
    @PreAuthorize("hasRole('STUDENT')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        logbookEntryService.deleteDraft(id, jwt.getSubject());
    }
}
