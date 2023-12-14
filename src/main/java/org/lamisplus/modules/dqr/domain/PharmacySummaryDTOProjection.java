package org.lamisplus.modules.dqr.domain;

public interface PharmacySummaryDTOProjection {

    Integer getRefillNumerator();
    Integer getRefillDenominator();
    Double getRefillPerformance();

    Integer getRegimenNumerator();
    Integer getRegimenDenominator();
    Double getRegimenPerformance();
}
