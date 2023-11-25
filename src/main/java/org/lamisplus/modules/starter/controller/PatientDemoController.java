package org.lamisplus.modules.starter.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lamisplus.modules.starter.domain.PatientDTOProjection;
import org.lamisplus.modules.starter.domain.PatientDto;
import org.lamisplus.modules.starter.service.StarterService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/dqr/")
public class PatientDemoController {

    private final StarterService starterService;

//    @GetMapping(value = "patient/enrollment", produces = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<List<PatientDto>> getAllP() {
//        return ResponseEntity.ok (hivEnrollmentService.getAll ());
//    }


    @GetMapping(value = "/patient-demo", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PatientDTOProjection>> patientDemo(@RequestParam("facilityId") Long facility) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(starterService.getPatient(facility));
    }
}
