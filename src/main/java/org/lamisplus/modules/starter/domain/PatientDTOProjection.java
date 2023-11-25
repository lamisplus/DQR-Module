package org.lamisplus.modules.starter.domain;

import org.codehaus.jackson.node.DoubleNode;

import java.util.Date;

public interface PatientDTOProjection {

    //basic information
    String getPatientId();
    String getHospitalNumber();
    String getGender();
    Date getDateOfBirth();

    String getStatus();

//    Integer getNumerator();
//
//    Integer getDenominator();
//
//    Double getPerformance();

}
