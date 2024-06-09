package org.lamisplus.modules.dqr.domain;

public interface PharmacySummaryDTOProjection {

    Integer getRefillNumerator();
    Integer getRefillDenominator();
    Integer getRefillVariance();
    Double getRefillPerformance();

    Integer getRegimenNumerator();
    Integer getRegimenDenominator();
    Integer getRegimenVariance();
    Double getRegimenPerformance();
}
