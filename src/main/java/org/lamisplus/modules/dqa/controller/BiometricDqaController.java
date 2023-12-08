package org.lamisplus.modules.dqa.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lamisplus.modules.dqa.domain.PatientDTOProjection;
import org.lamisplus.modules.dqa.domain.PatientSummaryDTOProjection;
import org.lamisplus.modules.dqa.service.BiometricDQAService;
import org.lamisplus.modules.dqa.service.CurrentUserOrganizationService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/dqr/")
public class BiometricDqaController {

private final BiometricDQAService biometricDQAService;
    private final CurrentUserOrganizationService organizationService;


    @GetMapping(value = "/biometric-error", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PatientDTOProjection>> getPatientsWithoutData(
            //@RequestParam("facilityId") Long facility,
            @RequestParam("indicator") String indicator
    ) throws ExecutionException, InterruptedException {
        Long facilityId = organizationService.getCurrentUserOrganization();
        List<PatientDTOProjection> result;

        switch (indicator) {
            case "bioDemo0":
                result = biometricDQAService.getNoBiometricCaptured(facilityId);
                break;
            case "bioDemo1":
                result = biometricDQAService.getNoValidBiometricCaptured(facilityId);
                break;
            case "bioDemo2":
                result = biometricDQAService.getNoRecaptureBiometric(facilityId);
                break;
            case "bioDemo3":
                result = biometricDQAService.getNoValidRecapture(facilityId);
                break;
            default:
                // Handle unknown dataType
                return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(result);
    }


}
