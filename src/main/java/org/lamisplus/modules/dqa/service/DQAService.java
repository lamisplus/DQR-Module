package org.lamisplus.modules.dqa.service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.lamisplus.modules.dqa.domain.PatientDTOProjection;
import org.lamisplus.modules.dqa.repository.PatientDemoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class DQAService {
    private final CurrentUserOrganizationService currentUserOrganizationService;
    private final PatientDemoRepository patientDemoRepository;

    public List<PatientDTOProjection> getPatient(Long facilityId) {
        //List<PatientDTOProjection> patientDemo = patientDemoRepository.getByPatientId(currentUserOrganizationService.getCurrentUserOrganization());
//        log.info("Size of the list {} ", patientDemo.size());
        return null; // patientDemo;
    }
}
