package org.lamisplus.modules.dqr.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lamisplus.modules.dqr.domain.PatientDTOProjection;
import org.lamisplus.modules.dqr.domain.TbSummaryDTOProjection;
import org.lamisplus.modules.dqr.repository.TbRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class TbDQAService {

    private final CurrentUserOrganizationService currentUserOrganizationService;
    private final TbRepository tbRepository;

    public List<TbSummaryDTOProjection> getTbSummary (Long facilityId) {
        List<TbSummaryDTOProjection> pTbSummary = tbRepository.getTbSummary (currentUserOrganizationService.getCurrentUserOrganization());
        return pTbSummary;
    }

    public List<PatientDTOProjection> getNoDocumentedTbScreening (Long facilityId) {
        List<PatientDTOProjection> cNoTbScreening = tbRepository.getPatientNoDocumentedTbScreening (currentUserOrganizationService.getCurrentUserOrganization());
        return cNoTbScreening;
    }

    public List<PatientDTOProjection> getNoTbScreeningOutCome (Long facilityId) {
        List<PatientDTOProjection> cNoTbScreeningOutCome = tbRepository.getPatientNoTbScreeningOutCome (currentUserOrganizationService.getCurrentUserOrganization());
        return cNoTbScreeningOutCome;
    }

    public List<PatientDTOProjection> getNoTbStatusLastVisit (Long facilityId) {
        List<PatientDTOProjection> cNoTbStatusLastVisit = tbRepository.getPatientTbStatusLastVisit (currentUserOrganizationService.getCurrentUserOrganization());
        return cNoTbStatusLastVisit;
    }

    public List<PatientDTOProjection> getNoTbSamplePresumptive (Long facilityId) {
        List<PatientDTOProjection> cNoTbSampleCollection = tbRepository.getPresumptiveNoTbSample (currentUserOrganizationService.getCurrentUserOrganization());
        return cNoTbSampleCollection;
    }

    public List<PatientDTOProjection> getNoTbSampleTypePresumptive (Long facilityId) {
        List<PatientDTOProjection> cNoTbSampleTypeCollection = tbRepository.getPresumptiveNoSampleType (currentUserOrganizationService.getCurrentUserOrganization());
        return cNoTbSampleTypeCollection;
    }

    public List<PatientDTOProjection> getOnTbWithoutOutcome (Long facilityId) {
        List<PatientDTOProjection> cTbClientWithoutOutcome = tbRepository.getPatientOnTbWithNoOutcome (currentUserOrganizationService.getCurrentUserOrganization());
        return cTbClientWithoutOutcome;
    }

    public List<PatientDTOProjection> getEligibleForIptNoDateStarted (Long facilityId) {
        List<PatientDTOProjection> cEligibleNotStarted = tbRepository.getPatientEligibleForIptNoDateIptStarted (currentUserOrganizationService.getCurrentUserOrganization());
        return cEligibleNotStarted;
    }

    public List<PatientDTOProjection> getEligibleForIptNoDateCompletion (Long facilityId) {
        List<PatientDTOProjection> cEligibleNotCompleted = tbRepository.getPatientIptStartedWithNoCompletionDate (currentUserOrganizationService.getCurrentUserOrganization());
        return cEligibleNotCompleted;
    }


    public List<PatientDTOProjection> getEligibleForIptNoDateCompletedStatus (Long facilityId) {
        List<PatientDTOProjection> cIptCompletedStatus = tbRepository.getPatientIptStartedWithoutCompletedStatus (currentUserOrganizationService.getCurrentUserOrganization());
        return cIptCompletedStatus;
    }

    public List<PatientDTOProjection> getCompletedIptType (Long facilityId) {
        List<PatientDTOProjection> cIptCompletedtype = tbRepository.getPatientIptStartedWithoutTptType (currentUserOrganizationService.getCurrentUserOrganization());
        return cIptCompletedtype;
    }
}
