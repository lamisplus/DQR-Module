package org.lamisplus.modules.dqr.domain;

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

    Integer getIptEliStartNumerator();
    Integer getIptEliStartDenominator();
    Double getIptEliStartPerformance();

    Integer getIpt6monthComplNumerator();
    Integer getIpt6monthComplDenominator();
    Double getIpt6monthComplPerformance();

    Integer getIptComplStatususNumerator();
    Integer getIptComplStatususDenominator();
    Double getIptComplStatususPerformance();

    Integer getIptTypeStatusNumerator();
    Integer getIptTypeStatusDenominator();
    Double getIptTypeStatusPerformance();


}
