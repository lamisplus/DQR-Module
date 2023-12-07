package org.lamisplus.modules.dqa.domain;

public interface BiometricSummaryDTOProjection {

    Integer getCaptureNumerator();
    Integer getCaptureDenominator();
    Double getCapturePerformance();

    Integer getValidCapNumerator();
    Integer getValidCapDenominator();
    Double getValidCapPerformance();

    Integer getRecapNumerator();
    Integer getRecapDenominator();
    Double getRecapPerformance();

    Integer getValidRecapNumerator();
    Integer getValidRecapDenominator();
    Double getValidRecapPerformance();
}
