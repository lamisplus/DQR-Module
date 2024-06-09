package org.lamisplus.modules.dqr.domain.entity;

public interface ValiditySummaryDTOProjection {

    Integer getBioNumerator();
    Integer getBioDenominator();
    Integer getBioVariance();
    Double getBioPerformance();

    Integer getRegimenNumerator();
    Integer getRegimenDenominator();
    Integer getRegimenVariance();
    Double getRegimenPerformance();

    Integer getVlDateNumerator();
    Integer getVlDateDenominator();
    Integer getVlDateVariance();
    Double getVlDatePerformance();

    Integer getHivDateNumerator();
    Integer getHivDateDenominator();
    Integer getHivDateVariance();
    Double getHivDatePerformance();

    Integer getStartDateNumerator();
    Integer getStartDateDenominator();
    Integer getStartDateVariance();
    Double getStartDatePerformance();

    Integer getAgeInitiatedNumerator();
    Integer getAgeInitiatedDenominator();
    Integer getAgeInitiatedVariance();
    Double getAgeInitiatedPerformance();

    Integer getNormalDobNumerator();
    Integer getNormalDobDenominator();
    Integer getNormalDobVariance();
    Double getNormalDobPerformance();


}
