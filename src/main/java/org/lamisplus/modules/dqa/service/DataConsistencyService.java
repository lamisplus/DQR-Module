package org.lamisplus.modules.dqa.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lamisplus.modules.dqa.domain.PatientDTOProjection;
import org.lamisplus.modules.dqa.domain.PatientSummaryDTOProjection;
import org.lamisplus.modules.dqa.repository.DataConsistencyRepository;
import org.lamisplus.modules.dqa.repository.DataValidityRepository;
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


    //                  Summary Block

    public List<PatientSummaryDTOProjection> getPatientTargetGroupSumm (Long facilityId) {
        List<PatientSummaryDTOProjection> pTargetGroupSumm = consistencyRepository.getTargetGroupSumm(currentUserOrganizationService.getCurrentUserOrganization());
        return pTargetGroupSumm;
    }

    public List<PatientSummaryDTOProjection> getPatientCareEntryPointSumm (Long facilityId) {
        List<PatientSummaryDTOProjection> pEntryPointSumm = consistencyRepository.getCareEntryPointSumm(currentUserOrganizationService.getCurrentUserOrganization());
        return pEntryPointSumm;
    }

    public List<PatientSummaryDTOProjection> getPatientAbnormalWeightSumm (Long facilityId) {
        List<PatientSummaryDTOProjection> pAbnormalWeightSumm = consistencyRepository.getAbnormalWeightGreaterThan121Summ(currentUserOrganizationService.getCurrentUserOrganization());
        return pAbnormalWeightSumm;
    }

    public List<PatientSummaryDTOProjection> getPaediatricAbnormalWeightSumm (Long facilityId) {
        List<PatientSummaryDTOProjection> pChildrenWeightSumm = consistencyRepository.getAbnormalWeightGreaterThan121Summ(currentUserOrganizationService.getCurrentUserOrganization());
        return pChildrenWeightSumm;
    }

    public List<PatientSummaryDTOProjection> getPatientPregStatusSumm (Long facilityId) {
        List<PatientSummaryDTOProjection> pFemalePregStatusSumm = consistencyRepository.getFemalePregStatusSumm(currentUserOrganizationService.getCurrentUserOrganization());
        return pFemalePregStatusSumm;
    }


}

