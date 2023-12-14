package org.lamisplus.modules.dqr.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lamisplus.modules.dqr.domain.TbSummaryDTOProjection;
import org.lamisplus.modules.dqr.repository.TbRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class TbDQAService {

    private final CurrentUserOrganizationService currentUserOrganizationService;
    private final TbRepository tbRepository;

    public List<TbSummaryDTOProjection> getTbSummary (Long facilityId) {
        List<TbSummaryDTOProjection> pTbSummary = tbRepository.getTbSummary (currentUserOrganizationService.getCurrentUserOrganization());
        return pTbSummary;
    }
}
