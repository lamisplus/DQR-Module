package org.lamisplus.modules.dqr.domain;

public interface EacDTOProjection {

    Integer getEacCommencedNumerator();
    Integer getEacCommencedDenominator();
    Integer getEacCommencedVariance();
    Double getEacCommencedPerformance();

    Integer getEacComDateNumerator();
    Integer getEacComDateDenominator();
    Integer getEacComDateVariance();
    Double getEacComDatePerformance();

    Integer getPostEacNumerator();
    Integer getPostEacDenominator();
    Integer getPostEacVariance();
    Double getPostEacPerformance();

}
