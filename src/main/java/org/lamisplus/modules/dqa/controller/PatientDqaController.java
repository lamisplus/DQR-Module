package org.lamisplus.modules.dqa.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lamisplus.modules.dqa.domain.*;
import org.lamisplus.modules.dqa.service.CurrentUserOrganizationService;
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
    private final CurrentUserOrganizationService organizationService;


    @GetMapping(value = "/patient-demo", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PatientDTOProjection>> getPatientsWithoutData(
            //@RequestParam("facilityId") Long facility,
            @RequestParam("indicator") String indicator
    ) throws ExecutionException, InterruptedException {
        Long facilityId = organizationService.getCurrentUserOrganization();
        List<PatientDTOProjection> result;

        switch (indicator) {
            case "patientDemo0":
                result = dqaService.getPatient(facilityId);
                break;
            case "patientDemo1":
                result = dqaService.getPatientWithoutAge(facilityId);
                break;
            case "patientDemo2":
                result = dqaService.getPatientWithoutSex(facilityId);
                break;
            case "patientDemo3":
                result = dqaService.getPatientWithoutMStatus(facilityId);
                break;
            case "patientDemo4":
                result = dqaService.getPatientWithoutEdu(facilityId);
                break;
            case "patientDemo5":
                result = dqaService.getPatientWithoutOccu(facilityId);
                break;
            case "patientDemo6":
                result = dqaService.getPatientWithoutAdd(facilityId);
                break;
            case "patientDemo7":
                result = dqaService.getPatientWithoutIdentifier(facilityId);
                break;
            default:
                // Handle unknown dataType
                return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(result);
    }


    //this section/code block below endpoints for summaries

    @GetMapping(value = "/patient-demo-summary", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<SummaryDTOProjection>>patientSummary() throws ExecutionException, InterruptedException {
        Long facility = organizationService.getCurrentUserOrganization();
        return  ResponseEntity.ok(dqaService.getPatientSummary(facility));
    }

    // clinical summary
    @GetMapping(value = "/clinical-variable-summary", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ClinicalSummaryDTOProjection>>clinicalSummary() throws ExecutionException, InterruptedException {
        Long facility = organizationService.getCurrentUserOrganization();
        return  ResponseEntity.ok(dqaService.getClinicaSummary(facility));
    }

    // biometric summary
    @GetMapping(value = "/biometric-summary", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<BiometricSummaryDTOProjection>> biometricSummary() throws ExecutionException, InterruptedException {
        Long facility = organizationService.getCurrentUserOrganization();
        return  ResponseEntity.ok(dqaService.getBiometricSummary(facility));
    }

    @GetMapping(value = "/pharmacy-summary", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PharmacySummaryDTOProjection>> pharmacySummary() throws ExecutionException, InterruptedException {
        Long facility = organizationService.getCurrentUserOrganization();
        return  ResponseEntity.ok(dqaService.getPharmacySummary(facility));
    }

    // data consistency summary
    @GetMapping(value = "/data-consistency-summary", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ClinicalConsistencyDTOProjection>> patientClinicalConsistencySummary () throws ExecutionException, InterruptedException {
        Long facility = organizationService.getCurrentUserOrganization();
        return ResponseEntity.ok(dqaService.getDataConsistencySummary(facility));
    }

    @GetMapping(value = "/prep-summary", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PrepSummaryDTOProjection>> prepSummary () throws ExecutionException, InterruptedException {
        Long facility = organizationService.getCurrentUserOrganization();
        return ResponseEntity.ok(dqaService.getPrepSummary(facility));
    }

    @GetMapping(value = "/hts-summary", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<HtsSummaryDTOProjection>> htsSummary () throws ExecutionException, InterruptedException {
        Long facility = organizationService.getCurrentUserOrganization();
        return ResponseEntity.ok(dqaService.getHtsSummary(facility));
    }
}
