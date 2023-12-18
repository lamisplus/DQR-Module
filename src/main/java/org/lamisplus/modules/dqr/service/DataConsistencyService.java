package org.lamisplus.modules.dqr.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lamisplus.modules.dqr.domain.PatientDTOProjection;
import org.lamisplus.modules.dqr.repository.DataConsistencyRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class DataConsistencyService {


   private final DataConsistencyRepository consistencyRepository;
    private final CurrentUserOrganizationService currentUserOrganizationService;

    //remember to implement complete data consistency

    public List<PatientDTOProjection> getPWithoutTargGroup (Long facilityId) {
        List<PatientDTOProjection> cWithoutTargetGroup = consistencyRepository.getPatientsWithoutTargetGroup(currentUserOrganizationService.getCurrentUserOrganization());
        return cWithoutTargetGroup;
    }

    public List<PatientDTOProjection> getPWithoutCareEntryPoint (Long facilityId) {
        List<PatientDTOProjection> cWithoutCareEntryPoint = consistencyRepository.getPatientsWithoutCareEntryPoint(currentUserOrganizationService.getCurrentUserOrganization());
        return cWithoutCareEntryPoint;
    }

    public List<PatientDTOProjection> getAbnormalWeight (Long facilityId) {
        List<PatientDTOProjection> cWithAbnormalWeight = consistencyRepository.getPatientsWithAbornormalWeightLastVisit(currentUserOrganizationService.getCurrentUserOrganization());
        return cWithAbnormalWeight;
    }

    public List<PatientDTOProjection> getChildrenAbnormalWeight (Long facilityId) {
        List<PatientDTOProjection> cAbnormalWeight = consistencyRepository.getPeadiatricWeightLastVisit(currentUserOrganizationService.getCurrentUserOrganization());
        return cAbnormalWeight;
    }

    public List<PatientDTOProjection> getPatientPregStatusLastVisit (Long facilityId) {
        List<PatientDTOProjection> cPregStatusLastVisit = consistencyRepository.getFemalePatientsWithoutPregStatusLastVisit(currentUserOrganizationService.getCurrentUserOrganization());
        return cPregStatusLastVisit;
    }

    public List<PatientDTOProjection> getPatientStartDateGreaterThanToday(Long facilityId) {
        List<PatientDTOProjection> cStartDateToday = consistencyRepository.getPatientStartDateGreaterThanToday(currentUserOrganizationService.getCurrentUserOrganization());
        return cStartDateToday;
    }

    public List<PatientDTOProjection> getPatientClinicDateGreaterThanToday(Long facilityId) {
        List<PatientDTOProjection> cClinicDateToday = consistencyRepository.getPatientClinicDateGreaterThanToday(currentUserOrganizationService.getCurrentUserOrganization());
        return cClinicDateToday;
    }

    public List<PatientDTOProjection> getArtDateGreaterThanClinicDay (Long facilityId) {
        List<PatientDTOProjection> cArtClinicDate = consistencyRepository.getPatientArtDateGreaterThanClinicDay(currentUserOrganizationService.getCurrentUserOrganization());
        return cArtClinicDate;
    }


    public List<PatientDTOProjection> getLastPickUpGreaterThanHivConfirmDate (Long facilityId) {
        List<PatientDTOProjection> lPickUpGreaterThanConfirmDate = consistencyRepository.getPatientLastPickUpGreaterThanConfirmDate(currentUserOrganizationService.getCurrentUserOrganization());
        return lPickUpGreaterThanConfirmDate;
    }

    public List<PatientDTOProjection> getArtStartGreaterThanTransferInDate (Long facilityId) {
        List<PatientDTOProjection> artStartGreaterThanConfirmDate = consistencyRepository.getPatientStartDateGreaterThanTransferIn(currentUserOrganizationService.getCurrentUserOrganization());
        return artStartGreaterThanConfirmDate;
    }


    public List<PatientDTOProjection> getDobGreaterThanLastPickUp (Long facilityId) {
        List<PatientDTOProjection> dobGreaterThanLastPickUp = consistencyRepository.getPatientDobGreaterThanLastPick(currentUserOrganizationService.getCurrentUserOrganization());
        return dobGreaterThanLastPickUp;
    }

    public List<PatientDTOProjection> getLastPickUpGreaterThanTransferIn (Long facilityId) {
        List<PatientDTOProjection> lPickUpGreaterThanTransferIn = consistencyRepository.getPatientLastPickUpGreaterThanTransferInDate(currentUserOrganizationService.getCurrentUserOrganization());
        return lPickUpGreaterThanTransferIn;
    }

    public List<PatientDTOProjection> getLastPickUpGreaterThanToday (Long facilityId) {
        List<PatientDTOProjection> lPickUpGreaterThanToday = consistencyRepository.getPatientLastPickUpGreaterThanToday(currentUserOrganizationService.getCurrentUserOrganization());
        return lPickUpGreaterThanToday;
    }

    public List<PatientDTOProjection> getLastClinicGreaterThanToday (Long facilityId) {
        List<PatientDTOProjection> lClinicGreaterThanToday = consistencyRepository.getPatientLastClinicGreaterThanToday(currentUserOrganizationService.getCurrentUserOrganization());
        return lClinicGreaterThanToday;
    }

    public List<PatientDTOProjection> getLastSampleDateGreaterThanResultDate (Long facilityId) {
        List<PatientDTOProjection> vlSampleDateGreaterThanResultDate = consistencyRepository.getPatientVlSampleDateGreaterThanResultDate(currentUserOrganizationService.getCurrentUserOrganization());
        return vlSampleDateGreaterThanResultDate;
    }




}

