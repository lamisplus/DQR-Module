package org.lamisplus.modules.dqr.domain;

public interface EacDTOProjection {

    Integer getEacCommencedNumerator();
    Integer getEacCommencedDenominator();
    Double getEacCommencedPerformance();

    Integer getEacComDateNumerator();
    Integer getEacComDateDenominator();
    Double getEacComDatePerformance();

    Integer getPostEacNumerator();
    Integer getPostEacDenominator();
    Double getPostEacPerformance();

}
