package org.lamisplus.modules.dqa.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lamisplus.modules.dqa.domain.PatientDTOProjection;
import org.lamisplus.modules.dqa.domain.PatientSummaryDTOProjection;
import org.lamisplus.modules.dqa.domain.SummaryDTOProjection;
import org.lamisplus.modules.dqa.service.DQAService;
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
public class PatientDqaController {
    private final DQAService dqaService;

    @GetMapping(value = "/no-dob", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PatientDTOProjection>> patientDemo(@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(dqaService.getPatient(facility));
    }

    @GetMapping(value = "/no-age", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PatientDTOProjection>> patientNoAge(@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(dqaService.getPatientWithoutAge(facility));
    }
    @GetMapping(value = "/no-sex", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PatientDTOProjection>> patientNoSex(@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(dqaService.getPatientWithoutSex(facility));
    }
    @GetMapping(value = "/no-marital-status", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PatientDTOProjection>> patientNoMar(@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(dqaService.getPatientWithoutMStatus(facility));
    }
    @GetMapping(value = "/no-education", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PatientDTOProjection>> patientNoEdu(@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(dqaService.getPatientWithoutEdu(facility));
    }
    @GetMapping(value = "/no-occupation", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PatientDTOProjection>> patientNoOcc(@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(dqaService.getPatientWithoutOccu(facility));
    }
    @GetMapping(value = "/no-address", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PatientDTOProjection>> patientNoAddress(@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(dqaService.getPatientWithoutAdd(facility));
    }

    @GetMapping(value = "/no-identifier", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PatientDTOProjection>> patientNoIdentifier(@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(dqaService.getPatientWithoutIdentifier(facility));
    }

    //this section/code block below endpoints for summaries
//    @GetMapping(value = "/dqa-dob-summary", produces = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<List<PatientSummaryDTOProjection>> dobSummary(@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
//        return ResponseEntity.ok(dqaService.getDobSummary(facility));
//    }

    @GetMapping(value = "/patient-demo-summary", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<SummaryDTOProjection>>ageSummary(@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
    return  ResponseEntity.ok(dqaService.getPatientSummary(facility));
    }
//
//    @GetMapping(value = "/dqa-sex-summary", produces = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<List<PatientSummaryDTOProjection>> sexSummary(@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
//        return  ResponseEntity.ok(dqaService.getSexSummary(facility));
//    }
//
//    @GetMapping(value = "/dqa-marrit-summary", produces = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<List<PatientSummaryDTOProjection>> marritalStaSummary(@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
//        return  ResponseEntity.ok(dqaService.getMarritalStaSummary(facility));
//    }
//
//    @GetMapping(value = "/dqa-education-summary", produces = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<List<PatientSummaryDTOProjection>> educationSummary(@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
//        return  ResponseEntity.ok(dqaService.getEducationalSummary(facility));
//    }
//
//    @GetMapping(value = "/dqa-occupation-summary", produces = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<List<PatientSummaryDTOProjection>> occupationSummary(@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
//        return  ResponseEntity.ok(dqaService.getOccupSummary(facility));
//    }
//
//    @GetMapping(value = "/dqa-address-summary", produces = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<List<PatientSummaryDTOProjection>> addressSummary(@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
//        return  ResponseEntity.ok(dqaService.getAddressSummary(facility));
//    }
//
//    @GetMapping(value = "/dqa-identifier-summary", produces = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<List<PatientSummaryDTOProjection>> identifierSummary(@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
//        return  ResponseEntity.ok(dqaService.getIdentifierSummary(facility));
//    }
}
