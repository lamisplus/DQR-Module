package org.lamisplus.modules.dqa.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lamisplus.modules.dqa.domain.PatientDTOProjection;
import org.lamisplus.modules.dqa.domain.PatientSummaryDTOProjection;
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



    // summary

    public List <PatientSummaryDTOProjection> getNotEnrolledSummary (Long facility) {
        List<PatientSummaryDTOProjection> pNotEnrolled = prepRepository.getNegativeNotEnrolledOnPrepSumm(currentUserOrganizationService.getCurrentUserOrganization());
        return pNotEnrolled;
    }

    public List <PatientSummaryDTOProjection> getNegativeOfferedSummary (Long facility) {
        List<PatientSummaryDTOProjection> pOfferedPrep= prepRepository.getNegativeOfferedPrepSumm(currentUserOrganizationService.getCurrentUserOrganization());
        return pOfferedPrep;
    }

    public List <PatientSummaryDTOProjection> getInitiatedOnPrepSummary (Long facility) {
        List<PatientSummaryDTOProjection> pInitiatedPrep= prepRepository.getInitiatedPrepSumm(currentUserOrganizationService.getCurrentUserOrganization());
        return pInitiatedPrep;
    }

    public List <PatientSummaryDTOProjection> getInitiatedWithUrinalysisPrepSummary (Long facility) {
        List<PatientSummaryDTOProjection> pInitiatedAndUrinalysisPrep= prepRepository.getInitiatedWithUrinalysisPrepSumm(currentUserOrganizationService.getCurrentUserOrganization());
        return pInitiatedAndUrinalysisPrep;
    }

//    public List <PatientSummaryDTOProjection> getInitiatedWithUrinalysisGreaterThanPrepSummary (Long facility) {
//        List<PatientSummaryDTOProjection> pInitiatedAndUrinalysisPrep= prepRepository.getCurrentUrinalysisGreaterThanHivStatusSumm(currentUserOrganizationService.getCurrentUserOrganization());
//        return pInitiatedAndUrinalysisPrep;
//    }

}
