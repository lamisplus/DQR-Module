package org.lamisplus.modules.dqr.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lamisplus.modules.dqr.domain.PatientDTOProjection;
import org.lamisplus.modules.dqr.repository.ClientVerificationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class ClientVerificationDQAService {

    private final CurrentUserOrganizationService currentUserOrganizationService;
    private final ClientVerificationRepository verificationRepository;


    public List<PatientDTOProjection> getIncompleteEncounters (Long facilityId) {
        List<PatientDTOProjection> cNoCompleteEnco = verificationRepository.getIncompleteEncounter (currentUserOrganizationService.getCurrentUserOrganization());
        return cNoCompleteEnco;
    }
    public List<PatientDTOProjection> getDuplicateClinicVisits (Long facilityId) {
        List<PatientDTOProjection> cClinicVisit = verificationRepository.getDuplicateClinicVisit(currentUserOrganizationService.getCurrentUserOrganization());
        return cClinicVisit;
    }

    public List<PatientDTOProjection> getDrugPickUpMoreThanOneYear (Long facilityId) {
        List<PatientDTOProjection> cDrugPickUpMoreAYear = verificationRepository.getLastPickMoreThanOneYear(currentUserOrganizationService.getCurrentUserOrganization());
        return cDrugPickUpMoreAYear;
    }

    public List<PatientDTOProjection> getClinicEncounterNoRecapture (Long facilityId) {
        List<PatientDTOProjection> cRecentClinicEncounter = verificationRepository.getRecentClinicEncounterNoRecapture(currentUserOrganizationService.getCurrentUserOrganization());
        return cRecentClinicEncounter;
    }

    public List<PatientDTOProjection> getLongClinic (Long facilityId) {
        List<PatientDTOProjection> cLongClinic = verificationRepository.getClinicGreaterThanOneFiveMonthYear(currentUserOrganizationService.getCurrentUserOrganization());
        return cLongClinic;
    }

    public List<PatientDTOProjection> getDuplicateDemo (Long facilityId) {
        List<PatientDTOProjection> cDuplicate = verificationRepository.getDuplicateDemo(currentUserOrganizationService.getCurrentUserOrganization());
        return cDuplicate;
    }
    public List<PatientDTOProjection> getNoBiometricBaseLine (Long facilityId) {
        List<PatientDTOProjection> cNoBaseLine = verificationRepository.getNoBaselineBiometric(currentUserOrganizationService.getCurrentUserOrganization());
        return cNoBaseLine;
    }

    public List<PatientDTOProjection> getNoRecaptureBiometric (Long facilityId) {
        List<PatientDTOProjection> cNoRecapture = verificationRepository.getNoRecaptureBiometric(currentUserOrganizationService.getCurrentUserOrganization());
        return cNoRecapture;
    }


}
