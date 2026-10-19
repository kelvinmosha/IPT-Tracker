
package tz.ac.dit.ipt_tracker.controller;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import tz.ac.dit.ipt_tracker.dto.OrganisationRequest;
import tz.ac.dit.ipt_tracker.dto.OrganisationResponse;
import tz.ac.dit.ipt_tracker.service.OrganisationService;

@RestController
@RequestMapping("/api/v1/organisations")
public class OrganisationController {

    private final OrganisationService service;

    public OrganisationController(OrganisationService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Page<OrganisationResponse> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(
                page,
                Math.min(Math.max(size, 1), 50),
                Sort.by("name").ascending());

        return service.findAll(pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public OrganisationResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @GetMapping("/search/region")
    @PreAuthorize("isAuthenticated()")
    public Page<OrganisationResponse> findByRegion(
            @RequestParam String region,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return service.findByRegion(
                region,
                PageRequest.of(page, Math.min(Math.max(size, 1), 50)));
    }

    @GetMapping("/search/sector")
    @PreAuthorize("isAuthenticated()")
    public Page<OrganisationResponse> findBySector(
            @RequestParam String sector,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return service.findBySector(
                sector,
                PageRequest.of(page, Math.min(Math.max(size, 1), 50)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public OrganisationResponse create(
            @Valid @RequestBody OrganisationRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public OrganisationResponse update(
            @PathVariable Long id,
            @Valid @RequestBody OrganisationRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
