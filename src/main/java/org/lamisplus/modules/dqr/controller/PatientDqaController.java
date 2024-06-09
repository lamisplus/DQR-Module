package org.lamisplus.modules.dqr.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lamisplus.modules.dqr.domain.*;
import org.lamisplus.modules.dqr.domain.entity.ValiditySummaryDTOProjection;
import org.lamisplus.modules.dqr.service.*;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.client.Client;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/dqr/")
public class PatientDqaController {
    private final DQAService dqaService;
    private final CurrentUserOrganizationService organizationService;
    private final PharmacyDQAService pharmacyDQAService;
    private final BiometricDQAService biometricDQAService;
    private final HtsDQAService htsDQAService;
    private final TbDQAService tbDQAService;
    private final LaboratoryDQAService laboratoryDQAService;
    private final DataValidityService validityService;
    private final EacDQAService eacDQAService;
    private final ClientVerificationDQAService verificationDQAService;


    @GetMapping(value = "/client-verification", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PatientDTOProjection>> getClientVerifications (
            @RequestParam("indicator") String indicator
    ) throws ExecutionException, InterruptedException {
        Long facilityId = organizationService.getCurrentUserOrganization();
        List<PatientDTOProjection> result;

        switch (indicator) {
            case "verify0":
                result = verificationDQAService.getNoBiometricBaseLine (facilityId);
                break;
            case "verify1":
                result = verificationDQAService.getNoRecaptureBiometric (facilityId);
                break;
            case "verify2":
                result = verificationDQAService.getDuplicateDemo (facilityId);
                break;
            case "verify3":
                result = verificationDQAService.getLongClinic (facilityId);
                break;
            case "verify4":
                result = verificationDQAService.getClinicEncounterNoRecapture (facilityId);
                break;
            case "verify5":
                result = verificationDQAService.getDrugPickUpMoreThanOneYear (facilityId);
                break;
            case "verify6":
                result = verificationDQAService.getDuplicateClinicVisits (facilityId);
                break;
            case "verify7":
                result =  verificationDQAService.getIncompleteEncounters (facilityId);
                break;
            case "verify8":
                result = verificationDQAService.getVlPrior (facilityId);
                break;
            default:
                // Handle unknown dataType
                return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(result);
    }
    @GetMapping(value = "/patient-tb", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PatientDTOProjection>> getPatientTbData (
            @RequestParam("indicator") String indicator
    ) throws ExecutionException, InterruptedException {
        Long facilityId = organizationService.getCurrentUserOrganization();
        List<PatientDTOProjection> result;

        switch (indicator) {
            case "tb0":
                result = tbDQAService.getNoDocumentedTbScreening(facilityId);
                break;
            case "tb1":
                result = tbDQAService.getNoTbScreeningOutCome(facilityId);
                break;
            case "tb2":
                result = tbDQAService.getNoTbStatusLastVisit(facilityId);
                break;
            case "tb3":
                result = tbDQAService.getNoTbSamplePresumptive(facilityId);
                break;
            case "tb4":
                result = tbDQAService.getNoTbSampleTypePresumptive(facilityId);
                break;
            case "tb5":
                result = tbDQAService.getOnTbWithoutOutcome(facilityId);
                break;
            case "tb6":
                result = tbDQAService.getEligibleForIptNoDateStarted(facilityId);
                break;
            case "tb7":
                result = tbDQAService.getEligibleForIptNoDateCompletion(facilityId);
                break;
            case "tb8":
                result = tbDQAService.getEligibleForIptNoDateCompletedStatus(facilityId);
                break;
            case "tb9":
                result = tbDQAService.getCompletedIptType(facilityId);
                break;
            default:
                // Handle unknown dataType
                return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(result);
    }


    //Laboratory Api's
    @GetMapping(value = "/patient-laboratory", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PatientDTOProjection>> getPatientLaboratoryData (
            @RequestParam("indicator") String indicator
    ) throws ExecutionException, InterruptedException {
        Long facilityId = organizationService.getCurrentUserOrganization();
        List<PatientDTOProjection> result;

        switch (indicator) {
            case "lab0":
                result = laboratoryDQAService.getEligibleNoVlResult(facilityId);
                break;
            case "lab1":
                result = laboratoryDQAService.getActiveNoVlResult(facilityId);
                break;
            case "lab2":
                result = laboratoryDQAService.getNoPcrDate(facilityId);
                break;
            case "lab3":
                result = laboratoryDQAService.getNoVlIndicator(facilityId);
                break;
            case "lab4":
                result = laboratoryDQAService.getVlSampleDateHigherThanResultDate(facilityId);
                break;
            case "lab5":
                result = laboratoryDQAService.getNoCd4WithinOneYear(facilityId);
                break;
            case "lab6":
                result = laboratoryDQAService.getNoInCd4WithinOneYear(facilityId);
                break;
            default:
                // Handle unknown dataType
                return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(result);
    }

    //Eac Api
    @GetMapping(value = "/patient-eac", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PatientDTOProjection>> getPatientEac (
            //@RequestParam("facilityId") Long facility,
            @RequestParam("indicator") String indicator
    ) throws ExecutionException, InterruptedException {
        Long facilityId = organizationService.getCurrentUserOrganization();
        List<PatientDTOProjection> result;

        switch (indicator) {
            case "eac0":
                result = eacDQAService.getPatientEacEligibleNotCommenced(facilityId);
                break;
            case "eac1":
                result = eacDQAService.getPatientEacNotCompleted(facilityId);
                break;
            case "eac2":
                result = eacDQAService.getPatientNoPostEacVl(facilityId);
                break;
            default:
                // Handle unknown dataType
                return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(result);
    }


    // data validity api
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
            case "validity6":
                result = validityService.getViralLodDateRange(facilityId);
                break;
            default:
                // Handle unknown dataType
                return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(result);
    }

    // pharmacy api
    @GetMapping(value = "/patient-pharmacy", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PatientDTOProjection>> getPatientPharmacy(
            //@RequestParam("facilityId") Long facility,
            @RequestParam("indicator") String indicator
    ) throws ExecutionException, InterruptedException {
        Long facilityId = organizationService.getCurrentUserOrganization();
        List<PatientDTOProjection> result;

        switch (indicator) {
            case "pharm0":
                result = pharmacyDQAService.getNoRegimenLastVisit(facilityId);
                break;
            case "pharm1":
                result = pharmacyDQAService.getNoDrugDuration(facilityId);
                break;
            default:
                // Handle unknown dataType
                return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(result);
    }

    // hts api
    @GetMapping(value = "/hts-data", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PatientDTOProjection>> getHtsData (
            //@RequestParam("facilityId") Long facility,
            @RequestParam("indicator") String indicator
    ) throws ExecutionException, InterruptedException {
        Long facilityId = organizationService.getCurrentUserOrganization();
        List<PatientDTOProjection> result;

        switch (indicator) {
            case "hts0":
                result = htsDQAService.getNoRecencyPos(facilityId);
                break;
            case "hts1":
                result = htsDQAService.getRecentNoVlSampleDate(facilityId);
                break;
            case "hts2":
                result = htsDQAService.getRecentNoVlSampleDateNoResult(facilityId);
                break;
            case "hts3":
                result = htsDQAService.getRecentVlGreaterThanReportDate(facilityId);
                break;
            case "hts4":
                result = htsDQAService.getRecentDateLessStatusDate(facilityId);
                break;
            case "hts5":
                result = htsDQAService.getPosNotElicited(facilityId);
                break;
            case "hts6":
                result = htsDQAService.getNoHtsTestSetting(facilityId);
                break;
            case "hts7":
                result = htsDQAService.getHtsNoTargetGroup(facilityId);
                break;
            default:
                // Handle unknown dataType
                return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(result);
    }

    //patient Demographic api
    @GetMapping(value = "/patient-demo", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PatientDTOProjection>> getPatientDemo (
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

    //Biometric Api's
    @GetMapping(value = "/biometric-error", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PatientDTOProjection>> getPatientBiometricData(
            //@RequestParam("facilityId") Long facility,
            @RequestParam("indicator") String indicator
    ) throws ExecutionException, InterruptedException {
        Long facilityId = organizationService.getCurrentUserOrganization();
        List<PatientDTOProjection> result;

        switch (indicator) {
            case "bioDemo0":
                result = biometricDQAService.getNoBiometricCaptured(facilityId);
                break;
            case "bioDemo1":
                result = biometricDQAService.getNoValidBiometricCaptured(facilityId);
                break;
            case "bioDemo2":
                result = biometricDQAService.getNoRecaptureBiometric(facilityId);
                break;
            case "bioDemo3":
                result = biometricDQAService.getNoValidRecapture(facilityId);
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

    //tb summary api
    @GetMapping(value = "/tb-summary", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<TbSummaryDTOProjection>> tbSummary () throws ExecutionException, InterruptedException {
        Long facility = organizationService.getCurrentUserOrganization();
        return ResponseEntity.ok(tbDQAService.getTbSummary(facility));
    }

    //vl summary api
    @GetMapping(value = "/laboratory-summary", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<LaboratoryDTOProjection>> labSummary () throws ExecutionException, InterruptedException {
        Long facility = organizationService.getCurrentUserOrganization();
        return ResponseEntity.ok(laboratoryDQAService.getLabsummary(facility));
    }

    // data validity Summary
    @GetMapping(value = "/validity-summary", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ValiditySummaryDTOProjection>> validitySummary () throws ExecutionException, InterruptedException {
        Long facility = organizationService.getCurrentUserOrganization();
        return ResponseEntity.ok(validityService.getValidityDataSummary(facility));
    }

    //eac summary
    @GetMapping(value = "/eac-summary", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<EacDTOProjection>> eacSummary () throws ExecutionException, InterruptedException {
        Long facility = organizationService.getCurrentUserOrganization();
        return ResponseEntity.ok(eacDQAService.getEacSummary(facility));
    }

    @GetMapping(value = "/verification-summary", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ClientVerificationDTOProjection>> verificationSummary () throws ExecutionException, InterruptedException {
        Long facility = organizationService.getCurrentUserOrganization();
        return ResponseEntity.ok(dqaService.getClientVerificationSummary(facility));
    }

}
