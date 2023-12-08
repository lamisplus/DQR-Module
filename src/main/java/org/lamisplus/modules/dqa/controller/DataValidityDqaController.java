package org.lamisplus.modules.dqa.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lamisplus.modules.dqa.domain.PatientDTOProjection;
import org.lamisplus.modules.dqa.service.CurrentUserOrganizationService;
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
    private final CurrentUserOrganizationService organizationService;

    @GetMapping(value = "/patient-validity", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PatientDTOProjection>> getPatientValidityData(
            @RequestParam("indicator") String indicator
    ) throws ExecutionException, InterruptedException {
        Long facilityId = organizationService.getCurrentUserOrganization();
        List<PatientDTOProjection> result;

        switch (indicator) {
            case "validity0":
                result = validityService.getPatientWithDobLessThanNineteenTwenty(facilityId);
                break;
            case "validity1":
                result = validityService.getPatientWithAgeBetweenZeroAndNinety(facilityId);
                break;
            case "validity2":
                result = validityService.getPatientWithArtStartDateLessThanNinetyEightyFive(facilityId);
                break;
            case "validity3":
                result = validityService.getPatientWithHivConfirmDateLessThanNinetyEightyFive(facilityId);
                break;
            case "validity4":
                result = validityService.getPatientWithoutValidBiometric(facilityId);
                break;
            case "validity5":
                result = validityService.getPatientNotWithinPeriod(facilityId);
                break;
//            case "validity6":
//                result = dqaService.getPatientWithoutAdd(facilityId);
//                break;
//            case "validity7":
//                result = dqaService.getPatientWithoutIdentifier(facilityId);
//                break;
            default:
                // Handle unknown dataType
                return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(result);
    }

}
