package org.lamisplus.modules.dqa.domain;

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
