package org.lamisplus.modules.dqr.domain;

public interface BiometricSummaryDTOProjection {

    Integer getCaptureNumerator();
    Integer getCaptureDenominator();

    Integer getCaptureVariance();
    Double getCapturePerformance();

    Integer getValidCapNumerator();
    Integer getValidCapDenominator();

    Integer getValidCapVariance();
    Double getValidCapPerformance();

    Integer getRecapNumerator();
    Integer getRecapDenominator();
    Integer getRecapVariance();
    Double getRecapPerformance();

    Integer getValidRecapNumerator();
    Integer getValidRecapDenominator();

    Integer getValidRecapVariance();
    Double getValidRecapPerformance();
}
