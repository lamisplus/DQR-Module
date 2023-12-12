package org.lamisplus.modules.dqa.domain.entity;

public interface ValiditySummaryDTOProjection {

    Integer getBioNumerator();
    Integer getBioDenominator();
    Double getBioPerformance();

    Integer getRegimenNumerator();
    Integer getRegimenDenominator();
    Double getRegimenPerformance();

    Integer getVlDateNumerator();
    Integer getVlDateDenominator();
    Double getVlDatePerformance();

    Integer getHivDateNumerator();
    Integer getHivDateDenominator();
    Double getHivDatePerformance();

    Integer getStartDateNumerator();
    Integer getStartDateDenominator();
    Double getStartDatePerformance();

    Integer getAgeInitiatedNumerator();
    Integer getAgeInitiatedDenominator();
    Double getAgeInitiatedPerformance();

    Integer getNormalDobNumerator();
    Integer getNormalDobDenominator();
    Double getNormalDobPerformance();


}
