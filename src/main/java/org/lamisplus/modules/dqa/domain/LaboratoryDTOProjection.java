package org.lamisplus.modules.dqa.domain;

public interface LaboratoryDTOProjection {

    Integer getEligibleVlNumerator();
    Integer getEligibleVlDenominator();
    Double getEligibleVlPerformance();

    Integer getHadVlNumerator();
    Integer getHadVlDenominator();
    Double getHadVlPerformance();

    Integer getHadPcrDateNumerator();
    Integer getHadPcrDateDenominator();
    Double getHadPcrDatePerformance();

    Integer getHadIndicatorNumerator();
    Integer getHadIndicatorDenominator();
    Double getHadIndicatorPerformance();

    Integer getVlDateGsDateNumerator();
    Integer getVlDateGsDateDenominator();
    Double getVlDateGsDatePerformance();

    Integer getTreatmentCd4Numerator();
    Integer getTreatmentCd4Denominator();
    Double getTreatmentCd4Performance();

    Integer getCd4WithinYearNumerator();
    Integer getCd4WithinYearDenominator();
    Double getCd4WithinYearPerformance();


}

