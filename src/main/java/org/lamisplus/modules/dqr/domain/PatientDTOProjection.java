package org.lamisplus.modules.dqr.domain;

import java.util.Date;
public interface PatientDTOProjection {

    //basic information
    String getPatientId();
    String getHospitalNumber();
    String getSex();
    Date getDateOfBirth();

    String getStatus();

//    Integer getNumerator();
//
//    Integer getDenominator();
//
//    Double getPerformance();
}
