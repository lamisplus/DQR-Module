package org.lamisplus.modules.starter.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lamisplus.modules.starter.domain.PatientDTOProjection;
import org.lamisplus.modules.starter.repository.PatientDemoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class StarterService {

    private final CurrentUserOrganizationService currentUserOrganizationService;
    private final PatientDemoRepository patientDemoRepository;

    public List<PatientDTOProjection> getPatient(Long facilityId) {
        List<PatientDTOProjection> patientDemo = patientDemoRepository.getPatient(currentUserOrganizationService.getCurrentUserOrganization());
//        log.info("Size of the list {} ", patientDemo.size());
        return patientDemo;
    }

}
