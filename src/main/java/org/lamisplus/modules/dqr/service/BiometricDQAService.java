package org.lamisplus.modules.dqr.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lamisplus.modules.dqr.domain.PatientDTOProjection;
import org.lamisplus.modules.dqr.repository.BiometricRepository;
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


}
