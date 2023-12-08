package org.lamisplus.modules.dqa.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lamisplus.modules.dqa.domain.ClinicalConsistencyDTOProjection;
import org.lamisplus.modules.dqa.domain.PatientDTOProjection;
import org.lamisplus.modules.dqa.domain.PatientSummaryDTOProjection;
import org.lamisplus.modules.dqa.service.CurrentUserOrganizationService;
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

    private final CurrentUserOrganizationService organizationService;

    @GetMapping(value = "/patient-consistency", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PatientDTOProjection>> getPatientConsistencyData(
            @RequestParam("indicator") String indicator
    ) throws ExecutionException, InterruptedException {
        Long facilityId = organizationService.getCurrentUserOrganization();
        List<PatientDTOProjection> result;

        switch (indicator) {
            case "DataCon0":
                result = consistencyService.getPWithoutTargGroup(facilityId);
                break;
            case "DataCon1":
                result = consistencyService.getPWithoutCareEntryPoint(facilityId);
                break;
            case "DataCon2":
                result = consistencyService.getAbnormalWeight(facilityId);
                break;
            case "DataCon3":
                result = consistencyService.getChildrenAbnormalWeight(facilityId);
                break;
            case "pDataCon4":
                result = consistencyService.getPatientPregStatusLastVisit(facilityId);
                break;
//            case "DataCon5":
//                result = dqaService.getPatientWithoutOccu(facilityId);
//                break;
//            case "DataCon6":
//                result = dqaService.getPatientWithoutAdd(facilityId);
//                break;
//            case "DataCon7":
//                result = dqaService.getPatientWithoutIdentifier(facilityId);
//                break;
            default:
                // Handle unknown dataType
                return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(result);
    }

//    @GetMapping(value = "/no-target-group", produces = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<List<PatientDTOProjection>> patientNoTargetGroup (@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
//        return ResponseEntity.ok(consistencyService.getPWithoutTargGroup(facility));
//    }
//
//    @GetMapping(value = "/no-care-entry", produces = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<List<PatientDTOProjection>> patientNoCareEntryPoint (@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
//        return ResponseEntity.ok(consistencyService.getPWithoutCareEntryPoint(facility));
//    }
//
//    @GetMapping(value = "/client-abnormal-weight", produces = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<List<PatientDTOProjection>> patientAbnormalWeight (@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
//        return ResponseEntity.ok(consistencyService.getAbnormalWeight(facility));
//    }
//
//    @GetMapping(value = "/peadiatric-abnormal-weight", produces = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<List<PatientDTOProjection>> patientPeadiatricAbnormalWeight (@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
//        return ResponseEntity.ok(consistencyService.getChildrenAbnormalWeight(facility));
//    }
//
//    @GetMapping(value = "/last-pregnant-status", produces = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<List<PatientDTOProjection>> patientPregnantStatusLastVisit (@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
//        return ResponseEntity.ok(consistencyService.getPatientPregStatusLastVisit(facility));
//    }



}
