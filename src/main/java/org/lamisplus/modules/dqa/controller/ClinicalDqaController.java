package org.lamisplus.modules.dqa.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lamisplus.modules.dqa.domain.PatientDTOProjection;
import org.lamisplus.modules.dqa.domain.PatientSummaryDTOProjection;
import org.lamisplus.modules.dqa.service.ClinicalDQAService;
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
public class ClinicalDqaController {

    private final ClinicalDQAService clinicalDQAService;
    private final CurrentUserOrganizationService organizationService;

    @GetMapping(value = "/patient-clinic", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PatientDTOProjection>> getPatientClinicalData(

            @RequestParam("indicator") String indicator
    ) throws ExecutionException, InterruptedException {
        Long facilityId = organizationService.getCurrentUserOrganization();
        List<PatientDTOProjection> result;

        switch (indicator) {
            case "clinic0":
                result = clinicalDQAService.getNoArtDate(facilityId);
                break;
            case "clinic1":
                result = clinicalDQAService.getNoLastArvClinic(facilityId);
                break;
            case "clinic2":
                result = clinicalDQAService.getNoHivDate(facilityId);
                break;
            case "clinic3":
                result = clinicalDQAService.getNoAge(facilityId);
                break;
            case "clinic4":
                result = clinicalDQAService.getNoTargGroup(facilityId);
                break;
            case "clinic5":
                result = clinicalDQAService.getNoEntryPoint(facilityId);
                break;
            case "clinic6":
                result = clinicalDQAService.getNoLastClinicDate(facilityId);
                break;
            case "clinic7":
                result = clinicalDQAService.getNoLastWeight(facilityId);
                break;
            case "clinic8":
                result = clinicalDQAService.getNoPregStatus(facilityId);
                break;
            case "clinic9":
                result = clinicalDQAService.getNoDateDiagnoseHiv(facilityId);
                break;
            case "clinic10":
                result = clinicalDQAService.getNoHivEnrollDate(facilityId);
                break;
            case "clinic11":
                result = clinicalDQAService.getNoCommencementDate(facilityId);
                break;
            default:
                // Handle unknown dataType
                return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(result);
    }


//    @GetMapping(value = "/no-art-date", produces = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<List<PatientDTOProjection>> patientNoArtDate(@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
//        return ResponseEntity.ok(clinicalDQAService.getNoArtDate(facility));
//    }
//
//    @GetMapping(value = "/no-month-arv-refill", produces = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<List<PatientDTOProjection>> patientHivDate(@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
//        return ResponseEntity.ok(clinicalDQAService.getNoLastArvClinic(facility));
//    }
//
//    @GetMapping(value = "/no-hiv-confirmed-test-date", produces = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<List<PatientDTOProjection>> patientWithNoConfirmDate(@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
//        return ResponseEntity.ok(clinicalDQAService.getNoHivDate(facility));
//    }
//
//    @GetMapping(value = "/no-age-art-start", produces = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<List<PatientDTOProjection>> patientWithNoAgeAtInitiation(@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
//        return ResponseEntity.ok(clinicalDQAService.getNoAge(facility));
//    }
//
//    @GetMapping(value = "/no-target-group", produces = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<List<PatientDTOProjection>> patientWithNoTargetGroup(@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
//        return ResponseEntity.ok(clinicalDQAService.getNoTargGroup(facility));
//    }
//
//    @GetMapping(value = "/no-care-entry-point", produces = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<List<PatientDTOProjection>> patientWithNoCareEntry(@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
//        return ResponseEntity.ok(clinicalDQAService.getNoEntryPoint(facility));
//    }
//
//    @GetMapping(value = "/no-last-clinic-visit", produces = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<List<PatientDTOProjection>> patientWithNoLastClinicVisit(@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
//        return ResponseEntity.ok(clinicalDQAService.getNoLastClinicDate(facility));
//    }
//
//    @GetMapping(value = "/no-last-weight", produces = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<List<PatientDTOProjection>> patientWithNoWeight(@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
//        return ResponseEntity.ok(clinicalDQAService.getNoLastWeight(facility));
//    }
//
//    @GetMapping(value = "/no-pregnant-status", produces = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<List<PatientDTOProjection>> patientWithNoPregnantStatus(@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
//        return ResponseEntity.ok(clinicalDQAService.getNoPregStatus(facility));
//    }
//
//    @GetMapping(value = "/no-hiv-diagnosis", produces = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<List<PatientDTOProjection>> patientWithNoHivDiagnosis(@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
//        return ResponseEntity.ok(clinicalDQAService.getNoDateDiagnoseHiv(facility));
//    }
//
//    @GetMapping(value = "/no-hiv-enrollment", produces = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<List<PatientDTOProjection>> patientWithNoHivEnrollment(@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
//        return ResponseEntity.ok(clinicalDQAService.getNoHivEnrollDate(facility));
//    }
//
//    @GetMapping(value = "/no-commencement-date", produces = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<List<PatientDTOProjection>> patientWithNoCommencementDate(@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
//        return ResponseEntity.ok(clinicalDQAService.getNoCommencementDate(facility));
//    }

    // summary block below

}
