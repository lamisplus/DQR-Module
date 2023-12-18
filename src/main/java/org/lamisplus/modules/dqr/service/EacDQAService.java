package org.lamisplus.modules.dqr.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lamisplus.modules.dqr.domain.EacDTOProjection;
import org.lamisplus.modules.dqr.domain.PatientDTOProjection;
import org.lamisplus.modules.dqr.repository.EacRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class EacDQAService {

    private final CurrentUserOrganizationService currentUserOrganizationService;

    private final EacRepository eacRepository;

    public List<EacDTOProjection> getEacSummary(Long facilityId) {
        List<EacDTOProjection> eacSummary = eacRepository.getEacSummary(currentUserOrganizationService.getCurrentUserOrganization());
//        log.info("Size of the list {} ", eacSummary.size());
        return  eacSummary;
    }

    public List<PatientDTOProjection> getPatientEacEligibleNotCommenced(Long facilityId) {
        List<PatientDTOProjection> pEligibleEacNotCommenced = eacRepository.getEligibleWithNoVlResult(currentUserOrganizationService.getCurrentUserOrganization());
        return  pEligibleEacNotCommenced;
    }

    public List<PatientDTOProjection> getPatientEacNotCompleted(Long facilityId) {
        List<PatientDTOProjection> pEacNotCompleted = eacRepository.getOnEacNotCompleted(currentUserOrganizationService.getCurrentUserOrganization());
        return  pEacNotCompleted;
    }


    public List<PatientDTOProjection> getPatientNoPostEacVl(Long facilityId) {
        List<PatientDTOProjection> noPostEacVl = eacRepository.getNoPostEacVl (currentUserOrganizationService.getCurrentUserOrganization());
        return  noPostEacVl;
    }
}
