package org.lamisplus.modules.dqa.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lamisplus.modules.dqa.domain.PatientDTOProjection;
import org.lamisplus.modules.dqa.domain.PatientSummaryDTOProjection;
import org.lamisplus.modules.dqa.service.ClinicalDQAService;
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
public class ClinicalDqaController {

    private final ClinicalDQAService clinicalDQAService;

    @GetMapping(value = "/no-art-date", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PatientDTOProjection>> patientNoArtDate(@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(clinicalDQAService.getNoArtDate(facility));
    }

    @GetMapping(value = "/no-month-arv-refill", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PatientDTOProjection>> patientHivDate(@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(clinicalDQAService.getNoLastArvClinic(facility));
    }

    @GetMapping(value = "/no-hiv-confirmed-test-date", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PatientDTOProjection>> patientWithNoConfirmDate(@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(clinicalDQAService.getNoHivDate(facility));
    }

    @GetMapping(value = "/no-age-art-start", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PatientDTOProjection>> patientWithNoAgeAtInitiation(@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(clinicalDQAService.getNoAge(facility));
    }

    @GetMapping(value = "/no-target-group", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PatientDTOProjection>> patientWithNoTargetGroup(@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(clinicalDQAService.getNoTargGroup(facility));
    }

    @GetMapping(value = "/no-care-entry-point", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PatientDTOProjection>> patientWithNoCareEntry(@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(clinicalDQAService.getNoEntryPoint(facility));
    }

    @GetMapping(value = "/no-last-clinic-visit", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PatientDTOProjection>> patientWithNoLastClinicVisit(@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(clinicalDQAService.getNoLastClinicDate(facility));
    }

    @GetMapping(value = "/no-last-weight", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PatientDTOProjection>> patientWithNoWeight(@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(clinicalDQAService.getNoLastWeight(facility));
    }

    @GetMapping(value = "/no-pregnant-status", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PatientDTOProjection>> patientWithNoPregnantStatus(@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(clinicalDQAService.getNoPregStatus(facility));
    }

    @GetMapping(value = "/no-hiv-diagnosis", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PatientDTOProjection>> patientWithNoHivDiagnosis(@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(clinicalDQAService.getNoDateDiagnoseHiv(facility));
    }

    @GetMapping(value = "/no-hiv-enrollment", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PatientDTOProjection>> patientWithNoHivEnrollment(@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(clinicalDQAService.getNoHivEnrollDate(facility));
    }

    @GetMapping(value = "/no-commencement-date", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PatientDTOProjection>> patientWithNoCommencementDate(@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(clinicalDQAService.getNoCommencementDate(facility));
    }

    // summary block below
    @GetMapping(value = "/arv-month-summary", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PatientSummaryDTOProjection>> summMonthOfArv(@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(clinicalDQAService.getArvMnthSumm(facility));
    }

    @GetMapping(value = "/art-start-summary", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PatientSummaryDTOProjection>> summArtStartDate(@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(clinicalDQAService.getStrtDaSumm(facility));
    }
    @GetMapping(value = "/hiv-confirm-date-summary", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PatientSummaryDTOProjection>> summHivConfirmDate(@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(clinicalDQAService.getConfirmHivTestSumm(facility));
    }

    @GetMapping(value = "/age-start-summary", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PatientSummaryDTOProjection>> summAgeArtStart(@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(clinicalDQAService.getAgeSumm(facility));
    }

    @GetMapping(value = "/target-group-summary", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PatientSummaryDTOProjection>> summTargetGroup(@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(clinicalDQAService.getTargGroupSumm(facility));
    }

    @GetMapping(value = "/care-entry-summary", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PatientSummaryDTOProjection>> summCareEntryPoint(@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(clinicalDQAService.getEntryPointSumm(facility));
    }

    @GetMapping(value = "/lastclinic-visit-summary", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PatientSummaryDTOProjection>> summLastClinicVisitDate(@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(clinicalDQAService.getLastVisitClinicSumm(facility));
    }

    @GetMapping(value = "/last-weight-summary", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PatientSummaryDTOProjection>> summLastWeight(@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(clinicalDQAService.getLastWeightSumm(facility));
    }

    @GetMapping(value = "/pregnant-status-summary", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PatientSummaryDTOProjection>> summPregStatus(@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(clinicalDQAService.getPregStatusSumm(facility));
    }

    @GetMapping(value = "/hiv-diagnosis-summary", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PatientSummaryDTOProjection>> summHivDiagnosis(@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(clinicalDQAService.getConfirmHivTestSumm(facility));
    }

    @GetMapping(value = "/hiv-enrollment-summary", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PatientSummaryDTOProjection>> summEnrollmentDate(@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(clinicalDQAService.getHivEnrollDateSumm(facility));
    }

    @GetMapping(value = "/commencement-date-summary", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PatientSummaryDTOProjection>> summCommencementDate (@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(clinicalDQAService.getCommenceDatSumm(facility));
    }


}
