package org.lamisplus.modules.dqr.domain;

public interface HtsSummaryDTOProjection {

    Integer getTotalPosNumerator();
    Integer getTotalPosDenominator();
//    Double getTotalPosPerformance();

    Integer getWithVLNumerator();
    Integer getWithVLDenominator();
    Double getWithVLPerformance();

    Integer getWithVlResNumerator();
    Integer getWithVlResDenominator();
    Double getWithVlResPerformance();

    Integer getRsGreaterNumerator();
    Integer getRsGreaterDenominator();
    Double getRsGreaterPerformance();

    Integer getRecencyNumerator();
    Integer getRecencyDenominator();
    Double getRecencyPerformance();

    Integer getElicitedNumerator();
    Integer getElicitedDenominator();
    Double getElicitedPerformance();

    Integer getSettingsNumerator();
    Integer getSettingsDenominator();
    Double getSettingsPerformance();
    Integer getTargNumerator();
    Integer getTargDenominator();
    Double getTargPerformance();
}
