package org.lamisplus.modules.dqa.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lamisplus.modules.dqa.domain.PatientDTOProjection;
import org.lamisplus.modules.dqa.service.DataValidityService;
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
public class DataValidityDqaController {

    private final DataValidityService validityService;

    @GetMapping(value = "/dob-notin-range", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PatientDTOProjection>> patientDobNotWithinRange(@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(validityService.getPatientWithDobLessThanNineteenTwenty(facility));
    }

    @GetMapping(value = "/age-not-range", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PatientDTOProjection>> patientAgeNotInRange(@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(validityService.getPatientWithAgeBetweenZeroAndNinety(facility));
    }

    @GetMapping(value = "/art-date-lesser", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PatientDTOProjection>> patientNoArtDateLesser(@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(validityService.getPatientWithArtStartDateLessThanNinetyEightyFive(facility));
    }

    @GetMapping(value = "/less-confirmed-date", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PatientDTOProjection>> patientNoLesserConfirmHivDate(@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(validityService.getPatientWithHivConfirmDateLessThanNinetyEightyFive(facility));
    }

    @GetMapping(value = "/no-valid-biometric", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PatientDTOProjection>> patientNoValidBiometric(@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(validityService.getPatientWithoutValidBiometric(facility));
    }

    @GetMapping(value = "/not-within-period", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PatientDTOProjection>> patientNotWithingPeriod(@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(validityService.getPatientNotWithinPeriod(facility));
    }

    // summary section below
}
