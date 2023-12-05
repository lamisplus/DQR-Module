package org.lamisplus.modules.dqa.service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.lamisplus.modules.dqa.domain.PatientDTOProjection;
import org.lamisplus.modules.dqa.domain.PatientSummaryDTOProjection;
import org.lamisplus.modules.dqa.domain.SummaryDTOProjection;
import org.lamisplus.modules.dqa.repository.PatientDemoRepository;
import org.lamisplus.modules.dqa.repository.PatientSummaryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class DQAService {
    private final CurrentUserOrganizationService currentUserOrganizationService;
    private final PatientDemoRepository patientDemoRepository;
    private final PatientSummaryRepository patientSummaryRepository;

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
//
//    public List <PatientSummaryDTOProjection> getAgeSummary (Long facility) {
//        List <PatientSummaryDTOProjection> pAge = patientSummaryRepository.getWithAgeSum(currentUserOrganizationService.getCurrentUserOrganization());
//        return pAge;
//    }
//
//    public List <PatientSummaryDTOProjection> getSexSummary (Long facility) {
//        List <PatientSummaryDTOProjection> pSex = patientSummaryRepository.getWithSexSum(currentUserOrganizationService.getCurrentUserOrganization());
//        return pSex;
//            }
//
//public List <PatientSummaryDTOProjection> getMarritalStaSummary (Long facility) {
//        List<PatientSummaryDTOProjection> pMarritalStatus = patientSummaryRepository.getWithMaritalStaSum(currentUserOrganizationService.getCurrentUserOrganization());
//        return pMarritalStatus;
//}
//
//    public List <PatientSummaryDTOProjection> getEducationalSummary (Long facility) {
//        List<PatientSummaryDTOProjection> pEducational = patientSummaryRepository.getWithEducationSumm(currentUserOrganizationService.getCurrentUserOrganization());
//        return pEducational;
//    }
//
//    public List <PatientSummaryDTOProjection> getOccupSummary (Long facility) {
//        List<PatientSummaryDTOProjection> pOccupation = patientSummaryRepository.getWithOccupSumm(currentUserOrganizationService.getCurrentUserOrganization());
//        return pOccupation;
//    }
//
//    public List <PatientSummaryDTOProjection> getAddressSummary (Long facility) {
//        List<PatientSummaryDTOProjection> pAddress = patientSummaryRepository.getWithAddressSumm(currentUserOrganizationService.getCurrentUserOrganization());
//        return pAddress;
//    }
//
//    public List <PatientSummaryDTOProjection> getIdentifierSummary (Long facility) {
//        List<PatientSummaryDTOProjection> pIdentifier = patientSummaryRepository.getWithIdentifier(currentUserOrganizationService.getCurrentUserOrganization());
//        return pIdentifier;
//    }


}
