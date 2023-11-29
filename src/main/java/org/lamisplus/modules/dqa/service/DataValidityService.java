package org.lamisplus.modules.dqa.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lamisplus.modules.dqa.domain.PatientDTOProjection;
import org.lamisplus.modules.dqa.repository.DataValidityRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class DataValidityService {

    private final DataValidityRepository validityRepository;
    private final CurrentUserOrganizationService currentUserOrganizationService;

    //remember to implement for laboratory on this section

    public List<PatientDTOProjection> getPatientWithDobLessThanNineteenTwenty (Long facilityId) {
        List<PatientDTOProjection> cWithoutWrongDob = validityRepository.getPatientsWithDateLessThan1920(currentUserOrganizationService.getCurrentUserOrganization());
        return cWithoutWrongDob;
    }


    public List<PatientDTOProjection> getPatientWithAgeBetweenZeroAndNinety (Long facilityId) {
        List<PatientDTOProjection> cWithoutZeroAndNinety = validityRepository.getPatientsWithAgeBetweenZeroAndNinety(currentUserOrganizationService.getCurrentUserOrganization());
        return cWithoutZeroAndNinety;
    }

    public List<PatientDTOProjection> getPatientWithArtStartDateLessThanNinetyEightyFive (Long facilityId) {
        List<PatientDTOProjection> cWithoutArtLessThan = validityRepository.getPatientsWithArtStartLessThan1985(currentUserOrganizationService.getCurrentUserOrganization());
        return cWithoutArtLessThan;
    }

    public List<PatientDTOProjection> getPatientWithHivConfirmDateLessThanNinetyEightyFive (Long facilityId) {
        List<PatientDTOProjection> cWithoutHivConfirmLessThan = validityRepository.getPatientsWithhivConfirmDateLessThan1985(currentUserOrganizationService.getCurrentUserOrganization());
        return cWithoutHivConfirmLessThan;
    }

    public List<PatientDTOProjection> getPatientWithoutValidBiometric (Long facilityId) {
        List<PatientDTOProjection> cWithoutValidBiometric = validityRepository.getPatientsWithValidBiometric(currentUserOrganizationService.getCurrentUserOrganization());
        return cWithoutValidBiometric;
    }

    public List<PatientDTOProjection> getPatientNotWithinPeriod (Long facilityId) {
        List<PatientDTOProjection> cWithoutRange = validityRepository.getPatientsWithArvRefillPeriodBetweennFourteenAndOneHundredAndEight(currentUserOrganizationService.getCurrentUserOrganization());
        return cWithoutRange;
    }
}
