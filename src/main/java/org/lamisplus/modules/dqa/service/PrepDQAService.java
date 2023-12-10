package org.lamisplus.modules.dqa.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lamisplus.modules.dqa.domain.PatientDTOProjection;
import org.lamisplus.modules.dqa.domain.PatientSummaryDTOProjection;
import org.lamisplus.modules.dqa.domain.PrepSummaryDTOProjection;
import org.lamisplus.modules.dqa.repository.PrepRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class PrepDQAService {

    private final CurrentUserOrganizationService currentUserOrganizationService;
    private final PrepRepository prepRepository;

    public List<PatientDTOProjection> getClientNotOfferedPrep (Long facilityId) {
        List<PatientDTOProjection> patientNotOfferedPrep = prepRepository.getOfferedPrep(currentUserOrganizationService.getCurrentUserOrganization());
//        log.info("Size of the list {} ", patientNotOfferedPrep.size());
        return  patientNotOfferedPrep;
    }

    public List<PatientDTOProjection> getClientNotAcceptedPrep (Long facilityId) {
        List<PatientDTOProjection> patientNotAcceptedPrep = prepRepository.getAcceptedOffer(currentUserOrganizationService.getCurrentUserOrganization());
        return patientNotAcceptedPrep;
    }

    public List<PatientDTOProjection> getClientNotInitiatedPrep (Long facilityId) {
        List<PatientDTOProjection> patientNotInitiatedOnPrep = prepRepository.getPrepInitiated(currentUserOrganizationService.getCurrentUserOrganization());
        return patientNotInitiatedOnPrep;
    }

    public List<PatientDTOProjection> getClientWithNoUrinalysis (Long facilityId) {
        List<PatientDTOProjection> patientWithNoUrinalysis = prepRepository.getPrepInitiatedAndUrinalysis(currentUserOrganizationService.getCurrentUserOrganization());
        return patientWithNoUrinalysis;
    }

    public List<PatientDTOProjection> getClientWithDateRegisterLessThanDateCommenced (Long facilityId) {
        List<PatientDTOProjection> patientWithDateRegLessThanCommencedDate = prepRepository.getDateRegisterLessThanDateCommenced(currentUserOrganizationService.getCurrentUserOrganization());
        return patientWithDateRegLessThanCommencedDate;
    }

    public List<PatientDTOProjection> getClientUrinalysisGreaterThanStatusDate (Long facilityId) {
        List<PatientDTOProjection> prepUrinalysisGreater = prepRepository.getUrinalysisGreaterThanStatusDate(currentUserOrganizationService.getCurrentUserOrganization());
        return prepUrinalysisGreater;
    }

    public List<PatientDTOProjection> getClientDateEnrollGreaterThanUrinalysis (Long facilityId) {
        List<PatientDTOProjection> patientpatientUrinalysisGreaterThanDateEnroll = prepRepository.getUrinalysisGreaterThanDateEnroll(currentUserOrganizationService.getCurrentUserOrganization());
        return patientpatientUrinalysisGreaterThanDateEnroll;
    }



    // summary



}
