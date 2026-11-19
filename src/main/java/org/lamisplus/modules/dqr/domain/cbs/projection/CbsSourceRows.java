package org.lamisplus.modules.dqr.domain.cbs.projection;

import java.util.Date;

/** Row shapes for the clinical tables CBS reads. Each matches one query in CbsClinicalRepository. */
public final class CbsSourceRows {
    private CbsSourceRows() {
    }

    public interface Person {
        String getUuid();
        String getHospitalNumber();
        String getSex();
        Date getDateOfBirth();
        Boolean getDobEstimated();
    }

    public interface Enrollment {
        String getUuid();
        String getPersonUuid();
        String getUniqueId();
        Date getDateConfirmedHiv();
        Date getDateOfRegistration();
        Date getDateStarted();
        String getStatusAtRegistration();
        Date getCreatedDate();
    }

    public interface Commencement {
        String getUuid();
        String getPersonUuid();
        Date getRecordDate();
        Date getCreatedDate();
        Long getRegimenId();
        Long getRegimenLineId();
        String getCd4AtStart();
        String getClinicalStage();
    }

    public interface ClinicVisit {
        String getUuid();
        String getPersonUuid();
        Date getVisitDate();
        Date getCreatedDate();
        Boolean getIsCommencement();
    }

    public interface DatedRecord {
        String getUuid();
        String getPersonUuid();
        Date getRecordDate();
        Date getCreatedDate();
    }

    public interface HtsResult {
        String getUuid();
        String getPersonUuid();
        Date getRecordDate();
        Date getCreatedDate();
        String getResult();
    }

    public interface Refill {
        String getUuid();
        String getPersonUuid();
        Date getVisitDate();
        Integer getRefillPeriod();
        Integer getRegimenLine();
        Date getCreatedDate();
    }

    public interface TrackerStatus {
        String getUuid();
        String getPersonUuid();
        String getHivStatus();
        Date getStatusDate();
        String getCauseOfDeath();
    }

    public interface PatientTracking {
        String getUuid();
        String getPersonUuid();
        Date getTrackingDate();
        Date getDateOfDeath();
        String getCauseOfDeath();
        Date getDateReturned();
    }

    public interface LabTest {
        String getUuid();
        String getPersonUuid();
        Integer getLabTestId();
        Integer getIndication();
        Date getSampleDate();
        String getSampleNumber();
        Date getResultDate();
        String getResultReported();
    }

    public interface DuplicateId {
        String getPersonUuid();
        String getIdType();
        String getIdValue();
        Integer getOtherPatients();
    }

    public interface OrphanRecord {
        String getSourceTable();
        String getUuid();
        String getPersonUuid();
        Date getRecordDate();
    }

    public interface User {
        String getUserName();
        String getFullName();
    }
}
