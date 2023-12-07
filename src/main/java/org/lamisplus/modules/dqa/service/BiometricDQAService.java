package org.lamisplus.modules.dqa.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lamisplus.modules.dqa.domain.PatientDTOProjection;
import org.lamisplus.modules.dqa.domain.PatientSummaryDTOProjection;
import org.lamisplus.modules.dqa.repository.BiometricRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class BiometricDQAService {

    private final CurrentUserOrganizationService currentUserOrganizationService;
    private final BiometricRepository biometricRepository;

    public List<PatientDTOProjection> getNoBiometricCaptured (Long facilityId) {
        List<PatientDTOProjection> cWithoutBiometric = biometricRepository.getPatientsNotCaptured(currentUserOrganizationService.getCurrentUserOrganization());
        return cWithoutBiometric;
    }

    public List<PatientDTOProjection> getNoValidBiometricCaptured (Long facilityId) {
        List<PatientDTOProjection> cWithoutValidBiometric = biometricRepository.getPatientsValidCaptured(currentUserOrganizationService.getCurrentUserOrganization());
        return cWithoutValidBiometric;
    }

    public List<PatientDTOProjection> getNoRecaptureBiometric (Long facilityId) {
        List<PatientDTOProjection> cWithoutRecaptureBiometric = biometricRepository.getPatientsNotRecaptured(currentUserOrganizationService.getCurrentUserOrganization());
        return cWithoutRecaptureBiometric;
    }

    public List<PatientDTOProjection> getNoValidRecapture (Long facilityId) {
        List<PatientDTOProjection> cWithoutValidRecapture = biometricRepository.getPatientsValidRecaptured(currentUserOrganizationService.getCurrentUserOrganization());
        return cWithoutValidRecapture;
    }


    // summary section/ code block below
//    public List<PatientSummaryDTOProjection> getBiometricCaptureSumm (Long facilityId) {
//        List<PatientSummaryDTOProjection> cWithCapturedSumm = biometricRepository.getPatientCapturedSumm(currentUserOrganizationService.getCurrentUserOrganization());
//        return cWithCapturedSumm;
//    }
//
//    public List<PatientSummaryDTOProjection> getValidCaptureSumm (Long facilityId) {
//        List<PatientSummaryDTOProjection> cWithValidCapturedSumm = biometricRepository.getPatientsValidCapturedSumm(currentUserOrganizationService.getCurrentUserOrganization());
//        return cWithValidCapturedSumm;
//    }
//
//    public List<PatientSummaryDTOProjection> getNotRecaptureSumm (Long facilityId) {
//        List<PatientSummaryDTOProjection> cWithRecapturedSumm = biometricRepository.getPatientsNotRecapturedSumm(currentUserOrganizationService.getCurrentUserOrganization());
//        return cWithRecapturedSumm;
//    }
//
//    public List<PatientSummaryDTOProjection> getValidRecaptureSumm (Long facilityId) {
//        List<PatientSummaryDTOProjection> cWithValidRecapturedSumm = biometricRepository.getPatientsValidRecapturedSumm(currentUserOrganizationService.getCurrentUserOrganization());
//        return cWithValidRecapturedSumm;
//    }

}
