package org.lamisplus.modules.dqr.domain;

public interface LaboratoryDTOProjection {

    Integer getEligibleVlNumerator();
    Integer getEligibleVlDenominator();
    Integer getEligibleVlVariance();
    Double getEligibleVlPerformance();

    Integer getHadVlNumerator();
    Integer getHadVlDenominator();
    Integer getHadVlVariance();
    Double getHadVlPerformance();

    Integer getHadPcrDateNumerator();
    Integer getHadPcrDateDenominator();
    Integer getHadPcrDateVariance();
    Double getHadPcrDatePerformance();

    Integer getHadIndicatorNumerator();
    Integer getHadIndicatorDenominator();
    Integer getHadIndicatorVariance();
    Double getHadIndicatorPerformance();

    Integer getVlDateGsDateNumerator();
    Integer getVlDateGsDateDenominator();
    Integer getVlDateGsDateVariance();
    Double getVlDateGsDatePerformance();

    Integer getTreatmentCd4Numerator();
    Integer getTreatmentCd4Denominator();
    Integer getTreatmentCd4Variance();
    Double getTreatmentCd4Performance();

    Integer getCd4WithinYearNumerator();
    Integer getCd4WithinYearDenominator();
    Integer getCd4WithinYearVariance();
    Double getCd4WithinYearPerformance();


}

