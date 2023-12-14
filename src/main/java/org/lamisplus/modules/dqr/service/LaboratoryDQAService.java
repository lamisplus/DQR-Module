package org.lamisplus.modules.dqr.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lamisplus.modules.dqr.domain.LaboratoryDTOProjection;
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


}
