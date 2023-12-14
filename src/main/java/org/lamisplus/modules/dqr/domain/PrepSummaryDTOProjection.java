package org.lamisplus.modules.dqr.domain;

public interface PrepSummaryDTOProjection {

    Integer getPOfferredNumerator();
    Integer getPOfferedDenominator();
    Double getPOfferredPerformance();

    Integer getPAcceptedNumerator();
    Integer getPAcceptedDenominator();
    Double getPAcceptedPerformance();

    Integer getPEnrollNumerator();
    Integer getPEnrollDenominator();
    Double getPEnrollPerformance();

    Integer getPEnrolledPrepUrinaNumerator();
    Integer getPEnrolledPrepUrinaDenominator();
    Double getPEnrolledPrepUrinaPerformance();

    Integer getPUrinaGreaterEnrollNumerator();
    Integer getPUrinaGreaterEnrollDenominator();
    Double getPUrinaGreaterEnrollPerformance();

    Integer getPUrinaGreaterStatusDateNumerator();
    Integer getPUrinaGreaterStatusDateDenominator();
    Double getPUrinaGreaterStatusDatePerformance();

    Integer getCommencedNumerator();
    Integer getCommencedDenominator();
    Double getCommencedPerformance();
}
