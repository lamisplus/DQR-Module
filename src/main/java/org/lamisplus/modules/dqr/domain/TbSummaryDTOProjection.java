package org.lamisplus.modules.dqr.domain;

public interface TbSummaryDTOProjection {

    Integer getTbScreenNumerator();
    Integer getTbScreenDenominator();
    Integer getTbScreenVariance();
    Double getTbScreenPerformance();

    Integer getDocAndCompletedNumerator();
    Integer getDocAndCompletedDenominator();
    Integer getDocAndCompletedVariance();
    Double getDocAndCompletedPerformance();

    Integer getTbstatusNumerator();
    Integer getTbstatusDenominator();
    Integer getTbStatusVariance();
    Double getTbstatusPerformance();

    Integer getPreSampleNumerator();
    Integer getPreSampleDenominator();
    Integer getPreSampleVariance();
    Double getPreSamplePerformance();

    Integer getPreSampleTypeNumerator();
    Integer getPreSampleTypeDenominator();
    Integer getPreSampleTypeVariance();
    Double getPreSampleTypePerformance();

    Integer getTptstartNumerator();
    Integer getTptstartDenominator();
    Integer getTptStartVariance();
    Double getTptstartPerformance();

    Integer getIptEliStartNumerator();
    Integer getIptEliStartDenominator();
    Integer getIptEliStartVariance();
    Double getIptEliStartPerformance();

    Integer getIpt6monthComplNumerator();
    Integer getIpt6monthComplDenominator();
    Integer getIpt6monthComplVariance();
    Double getIpt6monthComplPerformance();

    Integer getIptComplStatususNumerator();
    Integer getIptComplStatususDenominator();
    Integer getIptComplStatususVariance();
    Double getIptComplStatususPerformance();

    Integer getIptTypeStatusNumerator();
    Integer getIptTypeStatusDenominator();
    Integer getIptTypeStatusVariance();
    Double getIptTypeStatusPerformance();


}
