package org.lamisplus.modules.dqa.domain;

public interface PrepSummaryDTOProjection {

    Integer getPOfferredNumerator();
    Integer getPOfferedDenominator();
    Double getPOfferredPerformance();

    Integer getPAccepetedNumerator();
    Integer getPAccepetedDenominator();
    Double getPAccepetedPerformance();

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
