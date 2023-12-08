package org.lamisplus.modules.dqa.domain;

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

    Integer getHasIndexNumerator();
    Integer getHasIndexDenominator();
    Double getHasIndexPerformance();

    Integer getSettingsNumerator();
    Integer getSettingsDenominator();
    Double getSettingsPerformance();
    Integer getTargNumerator();
    Integer getTargDenominator();
    Double getTargPerformance();
}
