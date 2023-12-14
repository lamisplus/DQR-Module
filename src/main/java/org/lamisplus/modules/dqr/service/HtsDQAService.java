package org.lamisplus.modules.dqr.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lamisplus.modules.dqr.domain.PatientDTOProjection;
import org.lamisplus.modules.dqr.repository.HtsRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class HtsDQAService {

    private final CurrentUserOrganizationService currentUserOrganizationService;
    private final HtsRepository htsRepository;

    public List<PatientDTOProjection> getNoRecencyPos (Long facilityId) {
        List<PatientDTOProjection> htsPosNoRecency = htsRepository.getHtsPosNoRecency(currentUserOrganizationService.getCurrentUserOrganization());
        return htsPosNoRecency;
    }

    public List<PatientDTOProjection> getRecentNoVlSampleDate (Long facilityId) {
        List<PatientDTOProjection> htsRecentNoVl = htsRepository.getHtsPosRecentNoVlSampleDate(currentUserOrganizationService.getCurrentUserOrganization());
        return htsRecentNoVl;
    }

    public List<PatientDTOProjection> getRecentNoVlSampleDateNoResult (Long facilityId) {
        List<PatientDTOProjection> htsRecentNoVlRs = htsRepository.getHtsPosRecentNoVlSampleAndNoResultDate(currentUserOrganizationService.getCurrentUserOrganization());
        return htsRecentNoVlRs;
    }

    public List<PatientDTOProjection> getRecentVlGreaterThanReportDate (Long facilityId) {
        List<PatientDTOProjection> htsRecentVlRs = htsRepository.getHtsPosRecentVlResultDateGreaterThanReportDate(currentUserOrganizationService.getCurrentUserOrganization());
        return htsRecentVlRs;
    }

    public List<PatientDTOProjection> getRecentDateLessStatusDate (Long facilityId) {
        List<PatientDTOProjection> htsRecentDateLessStatusDate = htsRepository.getHtsPosRecencyDateLessThanStatusDate(currentUserOrganizationService.getCurrentUserOrganization());
        return htsRecentDateLessStatusDate;
    }

    public List<PatientDTOProjection> getPosNotElicited (Long facilityId) {
        List<PatientDTOProjection> htsPosElicitation = htsRepository.getHtsPosNoElicitation(currentUserOrganizationService.getCurrentUserOrganization());
        return htsPosElicitation;
    }

    public List<PatientDTOProjection> getNoHtsTestSetting (Long facilityId) {
        List<PatientDTOProjection> htsTestSettings = htsRepository.getHtsNoTestSettings(currentUserOrganizationService.getCurrentUserOrganization());
        return htsTestSettings;
    }

    public List<PatientDTOProjection> getHtsNoTargetGroup (Long facilityId) {
        List<PatientDTOProjection> htsTargetGroup = htsRepository.getHtsNoTargetGroup(currentUserOrganizationService.getCurrentUserOrganization());
        return htsTargetGroup;
    }
}
