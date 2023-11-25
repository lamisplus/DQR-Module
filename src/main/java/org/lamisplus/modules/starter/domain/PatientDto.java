package org.lamisplus.modules.starter.domain;

import lombok.Data;

import java.util.Date;

@Data
public class PatientDto {

    //Basic information
    private String patientId;
    private String hospitalNum;
    private String sex;
    private Date dateOfBirth;
    private String status;
    private Integer numerator;
    private Integer deNumerator;
    private Double performance;

}
