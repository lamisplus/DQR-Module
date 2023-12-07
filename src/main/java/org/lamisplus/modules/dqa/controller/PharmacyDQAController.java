package org.lamisplus.modules.dqa.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lamisplus.modules.dqa.domain.PatientDTOProjection;
import org.lamisplus.modules.dqa.domain.PatientSummaryDTOProjection;
import org.lamisplus.modules.dqa.service.PharmacyDQAService;
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
public class PharmacyDQAController {


    private final PharmacyDQAService pharmacyDQAService;

    @GetMapping(value = "/no-regimen-lastvisit", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PatientDTOProjection>> patientNoRegimenLastVisit(@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(pharmacyDQAService.getNoRegimenLastVisit(facility));
    }

    @GetMapping(value = "/no-drug-duration", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PatientDTOProjection>> patientNoDrugDuration(@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(pharmacyDQAService.getNoDrugDuration(facility));
    }

}
