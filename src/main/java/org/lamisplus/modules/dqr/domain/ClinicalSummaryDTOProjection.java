package org.lamisplus.modules.dqr.domain;

public interface ClinicalSummaryDTOProjection {
    Integer getRefillMonthNumerator();
    Integer getRefillMonthDenominator();
    Integer getRefillMonthVariance();
    Double getRefillMonthPerformance();

    Integer getRegimenNumerator();
    Integer getRegimenDenominator();
    Integer getRegimenVariance();
    Double getRegimenPerformance();

    Integer getStartDateNumerator();
    Integer getStartDateDenominator();
    Integer getStartDateVariance();
    Double getStartDatePerformance();

    Integer getConfirmDateNumerator();
    Integer getConfirmDateDenominator();
    Integer getConfirmDateVariance();
    Double getConfirmDatePerformance();

    Integer getTargNumerator();
    Integer getTargDenominator();
    Integer getTargVariance();
    Double getTargPerformance();

    Integer getEntryNumerator();
    Integer getEntryDenominator();
    Integer getEntryVariance();
    Double getEntryPerformance();

    Integer getCommencedNumerator();
    Integer getCommencedDenominator();
    Integer getCommencedVariance();
    Double getCommencedPerformance();

    Integer getEnrolledDateNumerator();
    Integer getEnrolledDateDenominator();
    Integer getEnrolledDateVariance();
    Double getEnrolledDatePerformance();

    Integer getDiagnoseNumerator();
    Integer getDiagnoseDenominator();
    Integer getDiagnoseVariance();
    Double getDiagnosePerformance();

    Integer getPregNumerator();
    Integer getPregDenominator();
    Integer getPregVariance();
    Double getPregPerformance();

    Integer getWeightNumerator();
    Integer getWeightDenominator();
    Integer getWeightVariance();
    Double getWeightPerformance();

    Integer getLastVisitNumerator();
    Integer getLastVisitDenominator();
    Integer getLastVisitVariance();
    Double getLastVisitPerformance();

    Integer getAgeNumerator();
    Integer getAgeDenominator();
    Integer getAgeVariance();
    Double getAgePerformance();

    Integer getLastPickNumerator();
    Integer getLastPickDenominator();
    Integer getLastPickVariance();
    Double getLastPickPerformance();
}
