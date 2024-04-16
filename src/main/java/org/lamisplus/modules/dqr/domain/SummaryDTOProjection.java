package org.lamisplus.modules.dqr.domain;

public interface SummaryDTOProjection {

    Integer getAgeNumerator();
    Integer getAgeDenominator();
    Integer getAgeVariance();
    Double getAgePerformance();

    Integer getSexNumerator();
    Integer getSexDenominator();
    Integer getSexVariance();
    Double getSexPerformance();

    Integer getDobNumerator();
    Integer getDobDenominator();
    Integer getDobVariance();
    Double getDobPerformance();

    Integer getMaritalNumerator();
    Integer getMaritalDenominator();
    Integer getMaritalVariance();
    Double getMaritalPerformance();

    Integer getEduNumerator();
    Integer getEduDenominator();
    Integer getEduVariance();
    Double getEduPerformance();

    Integer getEmployNumerator();
    Integer getEmployDenominator();
    Integer getEmployVariance();
    Double getEmployPerformance();

    Integer getAddressNumerator();
    Integer getAddressDenominator();
    Integer getAddressVariance();
    Double getAddressPerformance();

    Integer getPidNumerator();
    Integer getPidDenominator();
    Integer getPidVariance();
    Double getPidPerformance();
}
