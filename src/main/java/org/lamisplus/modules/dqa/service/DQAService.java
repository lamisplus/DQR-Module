package org.lamisplus.modules.dqa.service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.lamisplus.modules.dqa.domain.*;
import org.lamisplus.modules.dqa.repository.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class DQAService {
    private final CurrentUserOrganizationService currentUserOrganizationService;
    private final PatientDemoRepository patientDemoRepository;
    private final PatientSummaryRepository patientSummaryRepository;
    private final ClinicalSummaryRepository clinicalSummaryRepository;
    private final BiometricRepository biometricRepository;
    private final PharmacyRepository pharmacyRepository;
    private final DataConsistencyRepository dataConsistencyRepository;
    private final  PrepRepository prepRepository;
    private final HtsRepository htsRepository;


    public List<PatientDTOProjection> getPatient(Long facilityId) {
        List<PatientDTOProjection> patientDemo = patientDemoRepository.getPatientWithoutDob(currentUserOrganizationService.getCurrentUserOrganization());
        log.info("Size of the list {} ", patientDemo.size());
        return  patientDemo;
    }


    public List<PatientDTOProjection> getPatientWithoutSex(Long facilityId) {
        List<PatientDTOProjection> pWithouSex = patientDemoRepository.getPatientsWithoutSex(currentUserOrganizationService.getCurrentUserOrganization());
        log.info("Size of the list {} ", pWithouSex.size());
        return  pWithouSex;
    }

    public List<PatientDTOProjection> getPatientWithoutAge(Long facilityId) {
        List<PatientDTOProjection> pWithouAge = patientDemoRepository.getPatientWithoutAge(currentUserOrganizationService.getCurrentUserOrganization());
        log.info("Size of the list {} ", pWithouAge.size());
        return  pWithouAge;
    }
    public List<PatientDTOProjection> getPatientWithoutMStatus(Long facilityId) {
        List<PatientDTOProjection> pWithoutMaritalStat = patientDemoRepository.getPatientWithoutMaritalSta(currentUserOrganizationService.getCurrentUserOrganization());
        log.info("Size of the list {} ", pWithoutMaritalStat.size());
        return  pWithoutMaritalStat;
    }

    public List<PatientDTOProjection> getPatientWithoutOccu(Long facilityId) {
        List<PatientDTOProjection> pWithoutOccupation = patientDemoRepository.getPatientWithoutOcc(currentUserOrganizationService.getCurrentUserOrganization());
        log.info("Size of the list {} ", pWithoutOccupation.size());
        return  pWithoutOccupation;
    }

    public List<PatientDTOProjection> getPatientWithoutEdu(Long facilityId) {
        List<PatientDTOProjection> pWithoutEduation = patientDemoRepository.getPatientWithoutEduc(currentUserOrganizationService.getCurrentUserOrganization());
        log.info("Size of the list {} ", pWithoutEduation.size());
        return  pWithoutEduation;
    }

    public List<PatientDTOProjection> getPatientWithoutAdd(Long facilityId) {
        List<PatientDTOProjection> pWithoutAddress = patientDemoRepository.getPatientWithoutAddr(currentUserOrganizationService.getCurrentUserOrganization());
        log.info("Size of the list {} ", pWithoutAddress.size());
        return  pWithoutAddress;
    }

    public List<PatientDTOProjection> getPatientWithoutIdentifier (Long facilityId) {
        List<PatientDTOProjection> pWithoutIdenfier = patientDemoRepository.getPatientWithoutIdentifier(currentUserOrganizationService.getCurrentUserOrganization());
        return pWithoutIdenfier;
    }


    // this block/Section of code for Patient Demographic Summary
    public List<SummaryDTOProjection> getPatientSummary (Long facility) {
        List<SummaryDTOProjection> pDemograph = patientSummaryRepository.patientDemoSummary(currentUserOrganizationService.getCurrentUserOrganization());
        return pDemograph;
    }

    public List<ClinicalSummaryDTOProjection> getClinicaSummary (Long facility) {
        List<ClinicalSummaryDTOProjection> pClinicalVariables = clinicalSummaryRepository.getClinicalSummary(currentUserOrganizationService.getCurrentUserOrganization());
        return pClinicalVariables;
    }

    public List<BiometricSummaryDTOProjection> getBiometricSummary (Long facility) {
        List<BiometricSummaryDTOProjection> pBiometricSummary = biometricRepository.getBiometricSummary(currentUserOrganizationService.getCurrentUserOrganization());
        return pBiometricSummary;
    }

    public List<PharmacySummaryDTOProjection> getPharmacySummary (Long facility) {
        List<PharmacySummaryDTOProjection> pPharmacySummary = pharmacyRepository.getPharmacySummary(currentUserOrganizationService.getCurrentUserOrganization());
        return pPharmacySummary;
    }

    public List<ClinicalConsistencyDTOProjection> getDataConsistencySummary (Long facilityId) {
        List<ClinicalConsistencyDTOProjection> pDataConsistencySummary = dataConsistencyRepository.getClinicalConsistencySummary(currentUserOrganizationService.getCurrentUserOrganization());
        return pDataConsistencySummary;
    }
    public List <PrepSummaryDTOProjection> getPrepSummary (Long facility) {
        List<PrepSummaryDTOProjection> pSummary = prepRepository.getPrepSummary(currentUserOrganizationService.getCurrentUserOrganization());
        return pSummary;
    }

    public List <HtsSummaryDTOProjection> getHtsSummary (Long facility) {
        List<HtsSummaryDTOProjection> htsSummary = htsRepository.getHtsSummary(currentUserOrganizationService.getCurrentUserOrganization());
        return htsSummary;
    }


}
