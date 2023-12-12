package org.lamisplus.modules.dqa.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lamisplus.modules.dqa.domain.LaboratoryDTOProjection;
import org.lamisplus.modules.dqa.domain.PatientDTOProjection;
import org.lamisplus.modules.dqa.repository.LaboratoryRepository;
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
