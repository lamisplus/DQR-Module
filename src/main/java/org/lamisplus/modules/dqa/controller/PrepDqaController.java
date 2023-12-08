package org.lamisplus.modules.dqa.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lamisplus.modules.dqa.domain.PatientDTOProjection;
import org.lamisplus.modules.dqa.domain.PatientSummaryDTOProjection;
import org.lamisplus.modules.dqa.service.CurrentUserOrganizationService;
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
    private final CurrentUserOrganizationService organizationService;

    @GetMapping(value = "/prep-dqa", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PatientDTOProjection>> getPrepPatientData(
            @RequestParam("indicator") String indicator
    ) throws ExecutionException, InterruptedException {
        Long facilityId = organizationService.getCurrentUserOrganization();
        List<PatientDTOProjection> result;

        switch (indicator) {
            case "validity0":
                result = prepDQAService.getClientNotOfferedPrep(facilityId);
                break;
            case "validity1":
                result = prepDQAService.getClientNotAcceptedPrep(facilityId);
                break;
            case "validity2":
                result = prepDQAService.getClientNotInitiatedPrep(facilityId);
                break;
            case "validity3":
                result = prepDQAService.getClientWithNoUrinalysis(facilityId);
                break;
            case "validity4":
                result = prepDQAService.getClientWithDateRegisterLessThanDateCommenced(facilityId);
                break;
//            case "validity5":
//                result = validityService.getPatientNotWithinPeriod(facilityId);
//                break;
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
