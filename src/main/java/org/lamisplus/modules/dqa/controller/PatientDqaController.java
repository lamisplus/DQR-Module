package org.lamisplus.modules.dqa.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lamisplus.modules.dqa.domain.*;
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


    @GetMapping(value = "/patient-demo", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PatientDTOProjection>> getPatientsWithoutData(
            @RequestParam("facilityId") Long facility,
            @RequestParam("indicator") String indicator
    ) throws ExecutionException, InterruptedException {
        List<PatientDTOProjection> result;

        switch (indicator) {
            case "Proportion of all active patients with Date of Birth (DOB)":
                result = dqaService.getPatient(facility);
                break;
            case "Proportion of all active patients with Current Age":
                result = dqaService.getPatientWithoutAge(facility);
                break;
            case "Proportion of all active patients with Sex":
                result = dqaService.getPatientWithoutSex(facility);
                break;
            case "Proportion of all active patients with a documented marital status":
                result = dqaService.getPatientWithoutMStatus(facility);
                break;
            case "Proportion of all active patients with a documented educational Status":
                result = dqaService.getPatientWithoutEdu(facility);
                break;
            case "Proportion of all active patients with documented occupational status":
                result = dqaService.getPatientWithoutOccu(facility);
                break;
            case "Proportion of all active patients with registered address/LGA of residence":
                result = dqaService.getPatientWithoutAdd(facility);
                break;
            case "Proportion of all active patients with Patient Identifier":
                result = dqaService.getPatientWithoutIdentifier(facility);
                break;
            default:
                // Handle unknown dataType
                return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(result);
    }


//    @GetMapping(value = "/no-dob", produces = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<List<PatientDTOProjection>> patientDemo(@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
//        return ResponseEntity.ok(dqaService.getPatient(facility));
//    }
//
//    @GetMapping(value = "/no-age", produces = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<List<PatientDTOProjection>> patientNoAge(@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
//        return ResponseEntity.ok(dqaService.getPatientWithoutAge(facility));
//    }
//    @GetMapping(value = "/no-sex", produces = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<List<PatientDTOProjection>> patientNoSex(@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
//        return ResponseEntity.ok(dqaService.getPatientWithoutSex(facility));
//    }
//    @GetMapping(value = "/no-marital-status", produces = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<List<PatientDTOProjection>> patientNoMar(@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
//        return ResponseEntity.ok(dqaService.getPatientWithoutMStatus(facility));
//    }
//    @GetMapping(value = "/no-education", produces = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<List<PatientDTOProjection>> patientNoEdu(@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
//        return ResponseEntity.ok(dqaService.getPatientWithoutEdu(facility));
//    }
//    @GetMapping(value = "/no-occupation", produces = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<List<PatientDTOProjection>> patientNoOcc(@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
//        return ResponseEntity.ok(dqaService.getPatientWithoutOccu(facility));
//    }
//    @GetMapping(value = "/no-address", produces = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<List<PatientDTOProjection>> patientNoAddress(@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
//        return ResponseEntity.ok(dqaService.getPatientWithoutAdd(facility));
//    }
//
//    @GetMapping(value = "/no-identifier", produces = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<List<PatientDTOProjection>> patientNoIdentifier(@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
//        return ResponseEntity.ok(dqaService.getPatientWithoutIdentifier(facility));
//    }

    //this section/code block below endpoints for summaries

    @GetMapping(value = "/patient-demo-summary", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<SummaryDTOProjection>>patientSummary(@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
    return  ResponseEntity.ok(dqaService.getPatientSummary(facility));
    }

    // clinical summary
    @GetMapping(value = "/clinical-variable-summary", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ClinicalSummaryDTOProjection>>clinicalSummary(@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
        return  ResponseEntity.ok(dqaService.getClinicaSummary(facility));
    }

    // biometric summary
    @GetMapping(value = "/biometric-summary", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<BiometricSummaryDTOProjection>> biometricSummary(@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
        return  ResponseEntity.ok(dqaService.getBiometricSummary(facility));
    }

    @GetMapping(value = "/pharmacy-summary", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PatientSummaryDTOProjection>> pharmacySummary(@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
        return  ResponseEntity.ok(dqaService.getPharmacySummary(facility));
    }

    // data consistency summary
    @GetMapping(value = "/data-consistency-summary", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ClinicalConsistencyDTOProjection>> patientClinicalConsistencySummary (@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(dqaService.getDataConsistencySummary(facility));
    }

    @GetMapping(value = "/prep-summary", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PrepSummaryDTOProjection>> prepSummary (@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(dqaService.getPrepSummary(facility));
    }
}
