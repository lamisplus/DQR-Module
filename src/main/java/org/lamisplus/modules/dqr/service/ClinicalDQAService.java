package org.lamisplus.modules.dqr.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lamisplus.modules.dqr.domain.PatientDTOProjection;
import org.lamisplus.modules.dqr.repository.ClinicalRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class ClinicalDQAService {

    private final CurrentUserOrganizationService currentUserOrganizationService;
    private final ClinicalRepository clinicalRepository;


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

}
