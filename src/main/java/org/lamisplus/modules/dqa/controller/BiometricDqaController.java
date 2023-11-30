package org.lamisplus.modules.dqa.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lamisplus.modules.dqa.domain.PatientDTOProjection;
import org.lamisplus.modules.dqa.domain.PatientSummaryDTOProjection;
import org.lamisplus.modules.dqa.service.BiometricDQAService;
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

    @GetMapping(value = "/no-biometric-client", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PatientDTOProjection>> patientWithNoBiometricCaptured(@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(biometricDQAService.getNoBiometricCaptured(facility));
    }

    @GetMapping(value = "/no-valid-biometric", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PatientDTOProjection>> patientWithNoValidBiometricCaptured(@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(biometricDQAService.getNoValidBiometricCaptured(facility));
    }

    @GetMapping(value = "/no-recapture-biometric", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PatientDTOProjection>> patientWithNoRecaptureBiometric(@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(biometricDQAService.getNoRecaptureBiometric(facility));
    }

    @GetMapping(value = "/no-valid-recapture", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PatientDTOProjection>> patientWithNoValidRecaptureBiometric(@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(biometricDQAService.getNoValidRecapture(facility));
    }


    // summary
    @GetMapping(value = "/biometric-captured-summary", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PatientSummaryDTOProjection>> biometricCaptureSummary(@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
        return  ResponseEntity.ok(biometricDQAService.getBiometricCaptureSumm(facility));
    }

    @GetMapping(value = "/biometric-Valid-capture-summary", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PatientSummaryDTOProjection>> biometricValidCaptureSummary(@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
        return  ResponseEntity.ok(biometricDQAService.getValidCaptureSumm(facility));
    }

    @GetMapping(value = "/no-recapture-summary", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PatientSummaryDTOProjection>> biometricNoRecaptureSummary(@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
        return  ResponseEntity.ok(biometricDQAService.getNotRecaptureSumm(facility));
    }

    @GetMapping(value = "/valid-recapture-summary", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PatientSummaryDTOProjection>> biometricValidRecaptureSummary(@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
        return  ResponseEntity.ok(biometricDQAService.getValidRecaptureSumm(facility));
    }

}
