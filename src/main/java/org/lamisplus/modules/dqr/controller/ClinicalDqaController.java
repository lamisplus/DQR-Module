package org.lamisplus.modules.dqr.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lamisplus.modules.dqr.domain.PatientDTOProjection;
import org.lamisplus.modules.dqr.service.ClinicalDQAService;
import org.lamisplus.modules.dqr.service.CurrentUserOrganizationService;
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
            case "clinic12":
                result = clinicalDQAService.getNoMonthRefill(facilityId);
                break;
            default:
                // Handle unknown dataType
                return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(result);
    }

    // summary block below

}
