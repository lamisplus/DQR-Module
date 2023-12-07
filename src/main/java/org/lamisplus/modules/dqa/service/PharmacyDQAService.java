package org.lamisplus.modules.dqa.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lamisplus.modules.dqa.domain.PatientDTOProjection;
import org.lamisplus.modules.dqa.domain.PatientSummaryDTOProjection;
import org.lamisplus.modules.dqa.repository.PharmacyRepository;
import org.lamisplus.modules.dqa.repository.PharmacySummaryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class PharmacyDQAService {

    private final CurrentUserOrganizationService currentUserOrganizationService;
    private final PharmacyRepository pharmacyRepository;
    private final PharmacySummaryRepository pharmacySummaryRepository;

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
