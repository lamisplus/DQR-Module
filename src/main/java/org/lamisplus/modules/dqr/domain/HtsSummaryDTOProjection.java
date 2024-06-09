package org.lamisplus.modules.dqr.domain;

public interface HtsSummaryDTOProjection {

    Integer getTotalPosNumerator();
    Integer getTotalPosDenominator();
    Integer getTotalPosVariance();
//    Double getTotalPosPerformance();

    Integer getWithVLNumerator();
    Integer getWithVLDenominator();
    Integer getWithVLVariance();
    Double getWithVLPerformance();

    Integer getWithVlResNumerator();
    Integer getWithVlResDenominator();
    Integer getWithVlResVariance();
    Double getWithVlResPerformance();

    Integer getRsGreaterNumerator();
    Integer getRsGreaterDenominator();
    Integer getRsGreaterVariance();
    Double getRsGreaterPerformance();

    Integer getRecencyNumerator();
    Integer getRecencyDenominator();
    Integer getRecencyVariance();
    Double getRecencyPerformance();

    Integer getElicitedNumerator();
    Integer getElicitedDenominator();
    Integer getElicitedVariance();
    Double getElicitedPerformance();

    Integer getSettingsNumerator();
    Integer getSettingsDenominator();
    Integer getSettingsVariance();
    Double getSettingsPerformance();
    Integer getTargNumerator();
    Integer getTargDenominator();
    Integer getTargVariance();
    Double getTargPerformance();
}
