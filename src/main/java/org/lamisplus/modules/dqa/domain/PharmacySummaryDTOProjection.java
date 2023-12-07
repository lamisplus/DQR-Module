package org.lamisplus.modules.dqa.domain;

public interface PharmacySummaryDTOProjection {

    Integer getRefillNumerator();
    Integer getRefillDenominator();
    Double getRefillPerformance();

    Integer getRegimenNumerator();
    Integer getRegimenDenominator();
    Double getRegimenPerformance();
}
