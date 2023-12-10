package org.lamisplus.modules.dqa.domain;

public interface TbSummaryDTOProjection {

    Integer getTbScreenNumerator();
    Integer getTbScreenDenominator();
    Double getTbScreenPerformance();

    Integer getDocAndCompletedNumerator();
    Integer getDocAndCompletedDenominator();
    Double getDocAndCompletedPerformance();

    Integer getTbstatusNumerator();
    Integer getTbstatusDenominator();
    Double getTbstatusPerformance();

    Integer getPreSampleNumerator();
    Integer getPreSampleDenominator();
    Double getPreSamplePerformance();

    Integer getPreSampleTypeNumerator();
    Integer getPreSampleTypeDenominator();
    Double getPreSampleTypePerformance();

    Integer getTptstartNumerator();
    Integer getTptstartDenominator();
    Double getTptstartPerformance();


}
