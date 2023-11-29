package org.lamisplus.modules.dqa.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lamisplus.modules.dqa.domain.PatientDTOProjection;
import org.lamisplus.modules.dqa.domain.PatientSummaryDTOProjection;
import org.lamisplus.modules.dqa.repository.ClinicalRepository;
import org.lamisplus.modules.dqa.repository.ClinicalSummaryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class ClinicalDQAService {

    private final CurrentUserOrganizationService currentUserOrganizationService;
    private final ClinicalRepository clinicalRepository;
    private final ClinicalSummaryRepository clinicalSummaryRepository;

    public List <PatientDTOProjection> getNoArtDate (Long facilityId) {
        List<PatientDTOProjection> cWithoutArtDate = clinicalRepository.getClinicalWithoutArtDate(currentUserOrganizationService.getCurrentUserOrganization());
        return cWithoutArtDate;
    }

    public List <PatientDTOProjection> getNoHivDate (Long facilityId) {
        List<PatientDTOProjection> cWithoutHivDate = clinicalRepository.getClinicalWithoutHivDate(currentUserOrganizationService.getCurrentUserOrganization());
        return cWithoutHivDate;
    }

    public List <PatientDTOProjection> getNoHivEnrollDate (Long facilityId) {
        List<PatientDTOProjection> cWithoutHivEnrolDate = clinicalRepository.getClientWithoutHivEnrolDate(currentUserOrganizationService.getCurrentUserOrganization());
        return cWithoutHivEnrolDate;
    }

    public List <PatientDTOProjection> getNoTargGroup (Long facilityId) {
        List<PatientDTOProjection> cWithouTargetEntry = clinicalRepository.getClinicalWithoutTarg(currentUserOrganizationService.getCurrentUserOrganization());
        return cWithouTargetEntry;
    }

    public List <PatientDTOProjection> getNoEntryPoint (Long facilityId) {
        List<PatientDTOProjection> cWithouEntryPoint = clinicalRepository.getClinicalWithoutEntryPoint(currentUserOrganizationService.getCurrentUserOrganization());
        return cWithouEntryPoint;
    }

    public List <PatientDTOProjection> getNoCommencementDate (Long facilityId) {
        List<PatientDTOProjection> cWithouCommencementDate = clinicalRepository.getClientWithoutCommencementDate(currentUserOrganizationService.getCurrentUserOrganization());
        return cWithouCommencementDate;
    }



    public List <PatientDTOProjection> getNoDateDiagnoseHiv (Long facilityId) {
        List<PatientDTOProjection> cWithoutDateDiagnoseHiv = clinicalRepository.getClientWithoutDateDiagnoseHiv(currentUserOrganizationService.getCurrentUserOrganization());
        return cWithoutDateDiagnoseHiv;
    }

    public List <PatientDTOProjection> getNoPregStatus (Long facilityId) {
        List<PatientDTOProjection> cWithoutPregStatus = clinicalRepository.getClientWithoutPregStatus(currentUserOrganizationService.getCurrentUserOrganization());
        return cWithoutPregStatus;
    }

    public List <PatientDTOProjection> getNoLastWeight (Long facilityId) {
        List<PatientDTOProjection> cWithoutLastWeight = clinicalRepository.getClientWithoutWeightLastVisit(currentUserOrganizationService.getCurrentUserOrganization());
        return cWithoutLastWeight;
    }

    public List <PatientDTOProjection> getNoLastArvClinic (Long facilityId) {
        List<PatientDTOProjection> cWithoutLastArvClinic= clinicalRepository.getClientWithNoArvLastClinic(currentUserOrganizationService.getCurrentUserOrganization());
        return cWithoutLastArvClinic;
    }

    public List <PatientDTOProjection> getNoAge (Long facilityId) {
        List<PatientDTOProjection> cWithoutAge = clinicalRepository.getClientWithoutAge(currentUserOrganizationService.getCurrentUserOrganization());
        return cWithoutAge;
    }

    public List <PatientDTOProjection> getNoLastClinicDate (Long facilityId) {
        List<PatientDTOProjection> cWithoutLastClinicDate = clinicalRepository.getClientWithNoLastVisitDate(currentUserOrganizationService.getCurrentUserOrganization());
        return cWithoutLastClinicDate;
    }

    // summary

    public List <PatientSummaryDTOProjection> getArvMnthSumm (Long facilityId) {
        List<PatientSummaryDTOProjection> cArvSumm = clinicalSummaryRepository.getArvMnthSum(currentUserOrganizationService.getCurrentUserOrganization());
        return cArvSumm;
    }

    public List <PatientSummaryDTOProjection> getStrtDaSumm (Long facilityId) {
        List<PatientSummaryDTOProjection> cStartDateSumm = clinicalSummaryRepository.getStartDateSumm(currentUserOrganizationService.getCurrentUserOrganization());
        return cStartDateSumm;
    }

    public List <PatientSummaryDTOProjection> getConfirmHivTestSumm (Long facilityId) {
        List<PatientSummaryDTOProjection> cHivTestDateSumm = clinicalSummaryRepository.getHivConfirmTestDateSumm(currentUserOrganizationService.getCurrentUserOrganization());
        return cHivTestDateSumm;
    }

    public List <PatientSummaryDTOProjection> getTargGroupSumm (Long facilityId) {
        List<PatientSummaryDTOProjection> cHivTargGroupSumm = clinicalSummaryRepository.getTargetGroupSumm(currentUserOrganizationService.getCurrentUserOrganization());
        return cHivTargGroupSumm;
    }

    public List <PatientSummaryDTOProjection> getEntryPointSumm (Long facilityId) {
        List<PatientSummaryDTOProjection> cEntryPointSumm = clinicalSummaryRepository.getEntryPointSumm(currentUserOrganizationService.getCurrentUserOrganization());
        return cEntryPointSumm;
    }


    public List <PatientSummaryDTOProjection> getCommenceDatSumm (Long facilityId) {
        List<PatientSummaryDTOProjection> cCommenceDateSumm = clinicalSummaryRepository.getCommenceDateSumm(currentUserOrganizationService.getCurrentUserOrganization());
        return cCommenceDateSumm;
    }

    public List <PatientSummaryDTOProjection> getHivEnrollDateSumm (Long facilityId) {
        List<PatientSummaryDTOProjection> cHivEnrollDateSumm = clinicalSummaryRepository.getHivEnrollSumm(currentUserOrganizationService.getCurrentUserOrganization());
        return cHivEnrollDateSumm;
    }

    public List <PatientSummaryDTOProjection> getDiagnoseDateHivSumm (Long facilityId) {
        List<PatientSummaryDTOProjection> cHivDiagnoseDateSumm = clinicalSummaryRepository.getDateDiagnoseHivSumm(currentUserOrganizationService.getCurrentUserOrganization());
        return cHivDiagnoseDateSumm;
    }

    public List <PatientSummaryDTOProjection> getPregStatusSumm (Long facilityId) {
        List<PatientSummaryDTOProjection> cPregStatusSumm = clinicalSummaryRepository.getPregStatusSumm(currentUserOrganizationService.getCurrentUserOrganization());
        return cPregStatusSumm;
    }

    public List <PatientSummaryDTOProjection> getLastWeightSumm (Long facilityId) {
        List<PatientSummaryDTOProjection> cLastVisitWeightSumm = clinicalSummaryRepository.getWeightLastVisitSumm(currentUserOrganizationService.getCurrentUserOrganization());
        return cLastVisitWeightSumm;
    }

    public List <PatientSummaryDTOProjection> getLastVisitClinicSumm (Long facilityId) {
        List<PatientSummaryDTOProjection> cLastClinicVisitSumm = clinicalSummaryRepository.getClinicLastVisitSumm(currentUserOrganizationService.getCurrentUserOrganization());
        return cLastClinicVisitSumm;
    }

    public List <PatientSummaryDTOProjection> getAgeSumm (Long facilityId) {
        List<PatientSummaryDTOProjection> cAgeSumm = clinicalSummaryRepository.getClientAgeSumm(currentUserOrganizationService.getCurrentUserOrganization());
        return cAgeSumm;
    }

    public List <PatientSummaryDTOProjection> getNoLastVisitDateSumm (Long facilityId) {
        List<PatientSummaryDTOProjection> cNoLastVisitDateSumm = clinicalSummaryRepository.getClientWithNoLastVisitDateSumm(currentUserOrganizationService.getCurrentUserOrganization());
        return cNoLastVisitDateSumm;
    }

}
