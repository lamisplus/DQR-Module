package org.lamisplus.modules.dqr.domain;

public interface ClinicalConsistencyDTOProjection {

    Integer getTargNumerator();
    Integer getTargDenominator();
    Double getTargPerformance();

    Integer getEntryNumerator();
    Integer getEntryDenominator();
    Double getEntryPerformance();

    Integer getAdultWeightNumerator();
    Integer getAdultWeightDenominator();
    Double getAdultWeightPerformance();

    Integer getPeadWeightNumerator();
    Integer getPeadWeightDenominator();
    Double getPeadWeightPerformance();

    Integer getPregNumerator();
    Integer getPregDenominator();
    Double getPregPerformance();

    Integer getArtEqClinicNumerator();
    Integer getArtEqClinicDenominator();
    Double getArtEqClinicPerformance();

    Integer getArtEqLastPickupNumerator();
    Integer getArtEqLastPickupDenominator();
    Double getArtEqLastPickupPerformance();

    Integer getLGreaterConfNumerator();
    Integer getLGreaterConfDenominator();
    Double getLGreaterConfPerformance();

    Integer getArtGreaterTransNumerator();
    Integer getArtGreaterTransDenominator();
    Double getArtGreaterTransPerformance();

    Integer getLstPickGreaterDObNumerator();
    Integer getLstPickGreaterDObDenominator();
    Double getLstPickGreaterDObPerformance();

    Integer getLDrugPickHighNumerator();
    Integer getLDrugPickHighDenominator();
    Double getLDrugPickHighPerformance();

    Integer getLDrugPickHighTodayNumerator();
    Integer getLDrugPickHighTodayDenominator();
    Double getLDrugPickHighTodayPerformance();

    Integer getClinicPickLessTodayNumerator();
    Integer getClinicPickLessTodayDenominator();
    Double getClinicPickLessTodayPerformance();

    Integer getArtDateLessTodayNumerator();
    Integer getArtDateLessTodayDenominator();
    Double getArtDateLessTodayPerformance();

}
