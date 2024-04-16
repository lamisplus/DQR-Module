package org.lamisplus.modules.dqr.domain;

public interface ClinicalConsistencyDTOProjection {

    Integer getTargNumerator();
    Integer getTargDenominator();
    Integer getTargVariance();
    Double getTargPerformance();

    Integer getEntryNumerator();
    Integer getEntryDenominator();
    Integer getEntryVariance();
    Double getEntryPerformance();

    Integer getAdultWeightNumerator();
    Integer getAdultWeightDenominator();
    Integer getAdultWeightVariance();
    Double getAdultWeightPerformance();

    Integer getPeadWeightNumerator();
    Integer getPeadWeightDenominator();
    Integer getPeadWeightVariance();
    Double getPeadWeightPerformance();

    Integer getPregNumerator();
    Integer getPregDenominator();
    Integer getPregVariance();
    Double getPregPerformance();

    Integer getArtEqClinicNumerator();
    Integer getArtEqClinicDenominator();
    Integer getArtEqClinicVariance();
    Double getArtEqClinicPerformance();

    Integer getArtEqLastPickupNumerator();
    Integer getArtEqLastPickupDenominator();
    Integer getArtEqLastPickupVariance();
    Double getArtEqLastPickupPerformance();

    Integer getLGreaterConfNumerator();
    Integer getLGreaterConfDenominator();
    Integer getLGreaterConfVariance();
    Double getLGreaterConfPerformance();

    Integer getArtGreaterTransNumerator();
    Integer getArtGreaterTransDenominator();
    Integer getArtGreaterTransVariance();
    Double getArtGreaterTransPerformance();

    Integer getLstPickGreaterDObNumerator();
    Integer getLstPickGreaterDObDenominator();
    Integer getLstPickGreaterDObVariance();
    Double getLstPickGreaterDObPerformance();

    Integer getLDrugPickHighNumerator();
    Integer getLDrugPickHighDenominator();
    Integer getLDrugPickHighVariance();
    Double getLDrugPickHighPerformance();

    Integer getLDrugPickHighTodayNumerator();
    Integer getLDrugPickHighTodayDenominator();
    Integer getLDrugPickHighTodayVariance();
    Double getLDrugPickHighTodayPerformance();

    Integer getClinicPickLessTodayNumerator();
    Integer getClinicPickLessTodayDenominator();
    Integer getClinicPickLessTodayVariance();
    Double getClinicPickLessTodayPerformance();

    Integer getArtDateLessTodayNumerator();
    Integer getArtDateLessTodayDenominator();
    Integer getArtDateLessTodayVariance();
    Double getArtDateLessTodayPerformance();

    Integer getVlNumerator();
    Integer getVlDenominator();
    Integer getVlVariance();
    Double getVlPerformance();

}
