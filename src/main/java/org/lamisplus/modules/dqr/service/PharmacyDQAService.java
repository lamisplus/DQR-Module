package org.lamisplus.modules.dqr.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lamisplus.modules.dqr.domain.PatientDTOProjection;
import org.lamisplus.modules.dqr.repository.PharmacyRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class PharmacyDQAService {

    private final CurrentUserOrganizationService currentUserOrganizationService;
    private final PharmacyRepository pharmacyRepository;

    public List<PatientDTOProjection> getNoRegimenLastVisit (Long facilityId) {
        List<PatientDTOProjection> cWithouRegimen = pharmacyRepository.getPatientsWithoutRegimen(currentUserOrganizationService.getCurrentUserOrganization());
        return cWithouRegimen;
    }

    public List<PatientDTOProjection> getNoDrugDuration (Long facilityId) {
        List<PatientDTOProjection> cWithouDrugDuration = pharmacyRepository.getPatientsWithoutDrugRefillDuration(currentUserOrganizationService.getCurrentUserOrganization());
        return cWithouDrugDuration;
    }

    // summary code block below


}
