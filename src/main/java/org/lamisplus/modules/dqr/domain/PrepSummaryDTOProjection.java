package org.lamisplus.modules.dqr.domain;

public interface PrepSummaryDTOProjection {

    Integer getPOfferredNumerator();
    Integer getPOfferedDenominator();
    Integer getPOfferedVariance();
    Double getPOfferredPerformance();

    Integer getPAcceptedNumerator();
    Integer getPAcceptedDenominator();
    Integer getPAcceptedVariance();
    Double getPAcceptedPerformance();

    Integer getPEnrollNumerator();
    Integer getPEnrollDenominator();
    Integer getPEnrollVariance();
    Double getPEnrollPerformance();

    Integer getPEnrolledPrepUrinaNumerator();
    Integer getPEnrolledPrepUrinaDenominator();
    Integer getPEnrolledPrepUrinaVariance();
    Double getPEnrolledPrepUrinaPerformance();

    Integer getPUrinaGreaterEnrollNumerator();
    Integer getPUrinaGreaterEnrollDenominator();
    Integer getPUrinaGreaterEnrollVariance();
    Double getPUrinaGreaterEnrollPerformance();

    Integer getPUrinaGreaterStatusDateNumerator();
    Integer getPUrinaGreaterStatusDateDenominator();
    Integer getPUrinaGreaterStatusDateVariance();
    Double getPUrinaGreaterStatusDatePerformance();

    Integer getCommencedNumerator();
    Integer getCommencedDenominator();
    Integer getCommencedVariance();
    Double getCommencedPerformance();
}
