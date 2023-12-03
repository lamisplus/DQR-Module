package org.lamisplus.modules.dqa.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lamisplus.modules.dqa.domain.PatientDTOProjection;
import org.lamisplus.modules.dqa.domain.PatientSummaryDTOProjection;
import org.lamisplus.modules.dqa.service.DataConsistencyService;
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
public class DataConsistencyDqaController {

    private final DataConsistencyService consistencyService;

    @GetMapping(value = "/no-target-group", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PatientDTOProjection>> patientNoTargetGroup (@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(consistencyService.getPWithoutTargGroup(facility));
    }

    @GetMapping(value = "/no-care-entry", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PatientDTOProjection>> patientNoCareEntryPoint (@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(consistencyService.getPWithoutCareEntryPoint(facility));
    }

    @GetMapping(value = "/client-abnormal-weight", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PatientDTOProjection>> patientAbnormalWeight (@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(consistencyService.getAbnormalWeight(facility));
    }

    @GetMapping(value = "/peadiatric-abnormal-weight", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PatientDTOProjection>> patientPeadiatricAbnormalWeight (@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(consistencyService.getChildrenAbnormalWeight(facility));
    }

    @GetMapping(value = "/last-pregnant-status", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PatientDTOProjection>> patientPregnantStatusLastVisit (@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(consistencyService.getPatientPregStatusLastVisit(facility));
    }



    // summary API's for Data Consistency

    @GetMapping(value = "/target-group-summary", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PatientSummaryDTOProjection>> patientTargetGroupSumm (@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(consistencyService.getPatientTargetGroupSumm(facility));
    }

    @GetMapping(value = "/entry-point-summary", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PatientSummaryDTOProjection>> patientCareEntryPointSumm (@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(consistencyService.getPatientCareEntryPointSumm(facility));
    }

    @GetMapping(value = "/patient-weight-summary", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PatientSummaryDTOProjection>> patientAbnormalWeightSumm (@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(consistencyService.getPatientAbnormalWeightSumm(facility));
    }

    @GetMapping(value = "/peadiatric-weight-summary", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PatientSummaryDTOProjection>> peadiatricAbnormalWeightSumm (@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(consistencyService.getPaediatricAbnormalWeightSumm(facility));
    }

    @GetMapping(value = "/pregnancy-status-summary", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PatientSummaryDTOProjection>> pregnancyStatusSumm (@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(consistencyService.getPatientPregStatusSumm(facility));
    }
}
