package org.lamisplus.modules.dqa.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lamisplus.modules.dqa.domain.PatientDTOProjection;
import org.lamisplus.modules.dqa.domain.PatientSummaryDTOProjection;
import org.lamisplus.modules.dqa.service.PrepDQAService;
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
public class PrepDqaController {

    private final PrepDQAService prepDQAService;

    @GetMapping(value = "/not-offered-prep", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PatientDTOProjection>> patientNotOfferedPrep(@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(prepDQAService.getClientNotOfferedPrep(facility));
    }

    @GetMapping(value = "/not-accepted-prep", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PatientDTOProjection>> patientNotAcceptedPrep(@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(prepDQAService.getClientNotAcceptedPrep(facility));
    }

    @GetMapping(value = "/not-initiated-prep", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PatientDTOProjection>> patientNotInitiatedPrep(@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(prepDQAService.getClientNotInitiatedPrep(facility));
    }

    @GetMapping(value = "/no-urinalysis-prep", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PatientDTOProjection>> patientNotInitiatedNoUrinalysisPrep(@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(prepDQAService.getClientWithNoUrinalysis(facility));
    }

    @GetMapping(value = "/register-lessthan-commenced", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PatientDTOProjection>> patientNotDateRegisterLessThanDateCommenced(@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(prepDQAService.getClientWithDateRegisterLessThanDateCommenced(facility));
    }

    // summary
    @GetMapping(value = "/prep-enrolled-summary", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PatientSummaryDTOProjection>> prepEnrolledSummary(@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
        return  ResponseEntity.ok(prepDQAService.getNotEnrolledSummary(facility));
    }

    @GetMapping(value = "/prep-offered-summary", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PatientSummaryDTOProjection>> prepOfferedSummary(@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
        return  ResponseEntity.ok(prepDQAService.getNegativeOfferedSummary(facility));
    }

    @GetMapping(value = "/prep-initiated-summary", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PatientSummaryDTOProjection>> prepInitiatedSummary(@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
        return  ResponseEntity.ok(prepDQAService.getInitiatedOnPrepSummary(facility));
    }

    @GetMapping(value = "/prep-urinalysis-summary", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PatientSummaryDTOProjection>> prepWithUrinalysisSummary(@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
        return  ResponseEntity.ok(prepDQAService.getInitiatedWithUrinalysisPrepSummary(facility));
    }
}
