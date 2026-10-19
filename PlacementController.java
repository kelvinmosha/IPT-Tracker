
package tz.ac.dit.ipt_tracker.controller;

import java.util.List;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import tz.ac.dit.ipt_tracker.dto.PlacementRequest;
import tz.ac.dit.ipt_tracker.dto.PlacementResponse;
import tz.ac.dit.ipt_tracker.service.PlacementService;

@RestController
@RequestMapping("/api/v1/placements")
@RequiredArgsConstructor
public class PlacementController {

    private final PlacementService placementService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<PlacementResponse>> findAll(
            Authentication authentication) {

        return ResponseEntity.ok(
                placementService.findAllForUser(authentication.getName()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PlacementResponse> findById(
            @PathVariable Long id,
            Authentication authentication) {

        return ResponseEntity.ok(
                placementService.findByIdForUser(
                        id,
                        authentication.getName()));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PlacementResponse> create(
            @Valid @RequestBody PlacementRequest request) {

        PlacementResponse response = placementService.create(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PlacementResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody PlacementRequest request) {

        return ResponseEntity.ok(
                placementService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {

        placementService.delete(id);

        return ResponseEntity.noContent().build();
    }
}
