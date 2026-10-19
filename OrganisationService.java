
package tz.ac.dit.ipt_tracker.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tz.ac.dit.ipt_tracker.dto.OrganisationRequest;
import tz.ac.dit.ipt_tracker.dto.OrganisationResponse;
import tz.ac.dit.ipt_tracker.entity.Organisation;
import tz.ac.dit.ipt_tracker.exception.DuplicateResourceException;
import tz.ac.dit.ipt_tracker.exception.ResourceNotFoundException;
import tz.ac.dit.ipt_tracker.repository.OrganisationRepository;

@Service
@Transactional
public class OrganisationService {

    private final OrganisationRepository organisationRepository;

    public OrganisationService(
            OrganisationRepository organisationRepository) {
        this.organisationRepository = organisationRepository;
    }

    // Create a new organisation
    public OrganisationResponse create(OrganisationRequest request) {

        String name = request.name().trim();

        if (organisationRepository.existsByNameIgnoreCase(name)) {
            throw new DuplicateResourceException(
                    "Organisation name already exists"
            );
        }

        Organisation organisation = new Organisation();

        organisation.setName(name);
        organisation.setSector(request.sector());
        organisation.setRegion(request.region());
        organisation.setAddress(request.address());
        organisation.setContactEmail(request.contactEmail());

        Organisation savedOrganisation =
                organisationRepository.save(organisation);

        return toResponse(savedOrganisation);
    }

    // Retrieve all organisations
    @Transactional(readOnly = true)
    public List<OrganisationResponse> findAll() {

        return organisationRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // Retrieve organisations using pagination
    @Transactional(readOnly = true)
    public Page<OrganisationResponse> findAll(Pageable pageable) {

        return organisationRepository.findAll(pageable)
                .map(this::toResponse);
    }

    // Retrieve one organisation by ID
    @Transactional(readOnly = true)
    public OrganisationResponse findById(Long id) {

        Organisation organisation = getOrganisation(id);

        return toResponse(organisation);
    }

    // Update an existing organisation
    public OrganisationResponse update(
            Long id,
            OrganisationRequest request) {

        Organisation organisation = getOrganisation(id);
        String name = request.name().trim();

        organisationRepository.findByNameIgnoreCase(name)
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new DuplicateResourceException(
                                "Organisation name already exists"
                        );
                    }
                });

        organisation.setName(name);
        organisation.setSector(request.sector());
        organisation.setRegion(request.region());
        organisation.setAddress(request.address());
        organisation.setContactEmail(request.contactEmail());

        Organisation updatedOrganisation =
                organisationRepository.save(organisation);

        return toResponse(updatedOrganisation);
    }

    // Delete an organisation
    public void delete(Long id) {

        Organisation organisation = getOrganisation(id);

        organisationRepository.delete(organisation);
    }

    // Search organisations by region
    @Transactional(readOnly = true)
    public Page<OrganisationResponse> findByRegion(
            String region,
            Pageable pageable) {

        return organisationRepository
                .findByRegionIgnoreCase(region.trim(), pageable)
                .map(this::toResponse);
    }

    // Search organisations by sector
    @Transactional(readOnly = true)
    public Page<OrganisationResponse> findBySector(
            String sector,
            Pageable pageable) {

        return organisationRepository
                .findBySectorIgnoreCase(sector.trim(), pageable)
                .map(this::toResponse);
    }

    // Find an organisation or throw an exception
    private Organisation getOrganisation(Long id) {

        return organisationRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Organisation not found with ID: " + id
                        )
                );
    }

    // Convert entity into a response DTO
    private OrganisationResponse toResponse(
            Organisation organisation) {

        return new OrganisationResponse(
                organisation.getId(),
                organisation.getName(),
                organisation.getSector(),
                organisation.getRegion(),
                organisation.getAddress(),
                organisation.getContactEmail(),
                organisation.getCreatedAt()
        );
    }
}
