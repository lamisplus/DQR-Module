package org.lamisplus.modules.dqa.service;

import com.itextpdf.text.pdf.PRIndirectReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lamisplus.modules.dqa.domain.PatientDTOProjection;
import org.lamisplus.modules.dqa.domain.TbSummaryDTOProjection;
import org.lamisplus.modules.dqa.repository.TbRepository;
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
