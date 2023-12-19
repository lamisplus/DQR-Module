package org.lamisplus.modules.dqr.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lamisplus.modules.dqr.domain.LaboratoryDTOProjection;
import org.lamisplus.modules.dqr.domain.PatientDTOProjection;
import org.lamisplus.modules.dqr.repository.LaboratoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class LaboratoryDQAService {

    private final CurrentUserOrganizationService currentUserOrganizationService;
    private final LaboratoryRepository laboratoryRepository;

    public List<LaboratoryDTOProjection> getLabsummary(Long facilityId) {
        List<LaboratoryDTOProjection> labSummary = laboratoryRepository.getLaboratorySummary(currentUserOrganizationService.getCurrentUserOrganization());
        log.info("Size of the list {} ", labSummary.size());
        return  labSummary;
    }

    public List<PatientDTOProjection> getEligibleNoVlResult (Long facilityId) {
        List<PatientDTOProjection> cEligibleNoResult = laboratoryRepository.getEligibleWithNoVlResult(currentUserOrganizationService.getCurrentUserOrganization());
        return cEligibleNoResult;
    }

    public List<PatientDTOProjection> getActiveNoVlResult (Long facilityId) {
        List<PatientDTOProjection> cActiveNoResult = laboratoryRepository.getActiveNoVl(currentUserOrganizationService.getCurrentUserOrganization());
        return cActiveNoResult;
    }

    public List<PatientDTOProjection> getNoPcrDate (Long facilityId) {
        List<PatientDTOProjection> cVlNoPcrDate = laboratoryRepository.getHadVlNoPcrDate(currentUserOrganizationService.getCurrentUserOrganization());
        return cVlNoPcrDate;
    }

    public List<PatientDTOProjection> getNoVlIndicator (Long facilityId) {
        List<PatientDTOProjection> cVlNoVlIndicator = laboratoryRepository.getHadNoVlIndicator(currentUserOrganizationService.getCurrentUserOrganization());
        return cVlNoVlIndicator;
    }

    public List<PatientDTOProjection> getVlSampleDateHigherThanResultDate (Long facilityId) {
        List<PatientDTOProjection> cVlSampleDateGreaterThanResultDate = laboratoryRepository.getVlSampleDateGreaterThanVlReportDate(currentUserOrganizationService.getCurrentUserOrganization());
        return cVlSampleDateGreaterThanResultDate;
    }

    public List<PatientDTOProjection> getNoCd4WithinOneYear (Long facilityId) {
        List<PatientDTOProjection> cNoCd4WithinOneYear = laboratoryRepository.getVlWithinOneYearWithCD4(currentUserOrganizationService.getCurrentUserOrganization());
        return cNoCd4WithinOneYear;
    }

    public List<PatientDTOProjection> getNoInCd4WithinOneYear (Long facilityId) {
        List<PatientDTOProjection> cNoCd4InWithinOneYear = laboratoryRepository.getWithinOneYearWithNoCD4(currentUserOrganizationService.getCurrentUserOrganization());
        return cNoCd4InWithinOneYear;
    }


}
