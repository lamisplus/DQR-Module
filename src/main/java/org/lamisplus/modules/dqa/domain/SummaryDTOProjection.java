package org.lamisplus.modules.dqa.domain;

public interface SummaryDTOProjection {

    Integer getAgeNumerator();
    Integer getAgeDenominator();
    Double getAgePerformance();

    Integer getSexNumerator();
    Integer getSexDenominator();
    Double getSexPerformance();

    Integer getDobNumerator();
    Integer getDobDenominator();
    Double getDobPerformance();

    Integer getMaritalNumerator();
    Integer getMaritalDenominator();
    Double getMaritalPerformance();

    Integer getEduNumerator();
    Integer getEduDenominator();
    Double getEduPerformance();

    Integer getEmployNumerator();
    Integer getEmployDenominator();
    Double getEmployPerformance();

    Integer getAddressNumerator();
    Integer getAddressDenominator();
    Double getAddressPerformance();

    Integer getPidNumerator();
    Integer getPidDenominator();
    Double getPidPerformance();
}
