package org.lamisplus.modules.dqr.domain;

public interface ClientVerificationDTOProjection {

    Integer getNoBaseLineNumerator();
    Integer getNoBaseLineDenominator();
    Double getNoBaseLinePerformance();

    Integer getHasBaseLineNoRecaptureNumerator();
    Integer getHasBaseLineNoRecaptureDenominator();
    Double getHasBaseLineNoRecapturePerformance();

    Integer getSameDemographicsNumerator();
    Integer getSameDemographicsDenominator();
    Double getSameDemographicsPerformance();

    Integer getClinicMorethanOneYearNumerator();
    Integer getClinicMorethanOneYearDenominator();
    Double getClinicMorethanOneYearPerformance();

    Integer getPickUpOneYearNumerator();
    Integer getPickUpOneYearDenominator();
    Double getPickUpOneYearPerformance();

    Integer getClinicNoRecaptureNumerator();
    Integer getClinicNoRecaptureDenominator();
    Double getClinicNoRecapturePerformance();

    Integer getSameClinicalNumerator();
    Integer getSameClinicalDenominator();
    Double getSameClinicalPerformance();

    Integer getIncompleteNumerator();
    Integer getIncompleteDenominator();
    Double getIncompletePerformance();
//
//    Integer getCaptureNumerator();
//    Integer getCaptureDenominator();
//    Double getCapturePerformance();
//
//    Integer getCaptureNumerator();
//    Integer getCaptureDenominator();
//    Double getCapturePerformance();
//
//    Integer getCaptureNumerator();
//    Integer getCaptureDenominator();
//    Double getCapturePerformance();


}
