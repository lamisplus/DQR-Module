package org.lamisplus.modules.dqa.domain;

public interface ClinicalSummaryDTOProjection {
    Integer getRefillMonthNumerator();
    Integer getRefillMonthDenominator();
    Double getRefillMonthPerformance();

    Integer getRegimenNumerator();
    Integer getRegimenDenominator();
    Double getRegimenPerformance();

    Integer getStartDateNumerator();
    Integer getStartDateDenominator();
    Double getStartDatePerformance();

    Integer getConfirmDateNumerator();
    Integer getConfirmDateDenominator();
    Double getConfirmDatePerformance();

    Integer getTargNumerator();
    Integer getTargDenominator();
    Double getTargPerformance();

    Integer getEntryNumerator();
    Integer getEntryDenominator();
    Double getEntryPerformance();

    Integer getCommencedNumerator();
    Integer getCommencedDenominator();
    Double getCommencedPerformance();

    Integer getEnrolledDateNumerator();
    Integer getEnrolledDateDenominator();
    Double getEnrolledDatePerformance();

    Integer getDiagnoseNumerator();
    Integer getDiagnoseDenominator();
    Double getDiagnosePerformance();

    Integer getPregNumerator();
    Integer getPregDenominator();
    Double getPregPerformance();

    Integer getWeightNumerator();
    Integer getWeightDenominator();
    Double getWeightPerformance();

    Integer getLastVisitNumerator();
    Integer getLastVisitDenominator();
    Double getLastVisitPerformance();

    Integer getAgeNumerator();
    Integer getAgeDenominator();
    Double getAgePerformance();

    Integer getLastPickNumerator();
    Integer getLastPickDenominator();
    Double getLastPickPerformance();
}
