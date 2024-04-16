package org.lamisplus.modules.dqr.domain;

public interface ClientVerificationDTOProjection {

    Integer getNoBaseLineNumerator();
    Integer getNoBaseLineDenominator();
    Integer getNoBaselineVariance();
    Double getNoBaseLinePerformance();

    Integer getHasBaseLineNoRecaptureNumerator();
    Integer getHasBaseLineNoRecaptureDenominator();
    Integer getHasBaseLineNoRecaptureVariance();
    Double getHasBaseLineNoRecapturePerformance();

    Integer getSameDemographicsNumerator();
    Integer getSameDemographicsDenominator();
    Integer getSameDemographicsVariance();
    Double getSameDemographicsPerformance();

    Integer getClinicMoreThanOneYearNumerator();
    Integer getClinicMoreThanOneYearDenominator();
    Integer getClinicMoreThanOneYearVariance();
    Double getClinicMoreThanOneYearPerformance();

    Integer getPickUpOneYearNumerator();
    Integer getPickUpOneYearDenominator();
    Integer getPickUpOneYearVariance();
    Double getPickUpOneYearPerformance();

    Integer getClinicNoRecaptureNumerator();
    Integer getClinicNoRecaptureDenominator();
    Integer getClinicNoRecaptureVariance();
    Double getClinicNoRecapturePerformance();

    Integer getSameClinicalNumerator();
    Integer getSameClinicalDenominator();
    Integer getSameClinicalVariance();
    Double getSameClinicalPerformance();

    Integer getIncompleteNumerator();
    Integer getIncompleteDenominator();
    Integer getIncompleteVariance();
    Double getIncompletePerformance();

    Integer getLabNumerator();
    Integer getLabDenominator();
    Integer getLabVariance();
    Double getLabPerformance();


}
