package org.lamisplus.modules.dqr.service.cbs;

import lombok.RequiredArgsConstructor;
import org.lamisplus.modules.dqr.domain.cbs.PatientSource;
import org.lamisplus.modules.dqr.domain.cbs.projection.CbsSourceRows;
import org.lamisplus.modules.dqr.repository.cbs.CbsClinicalRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Builds a PatientSource per patient from the clinical tables. Read-only: it only calls
 * the SELECT queries in CbsClinicalRepository.
 */
@Service
@RequiredArgsConstructor
public class CbsSourceLoader {

    private static final Pattern NUMBER = Pattern.compile("([0-9]+(\\.[0-9]+)?)");
    private static final int LAB_TEST_VIRAL_LOAD = 16;

    private final CbsClinicalRepository clinical;

    /** All CBS-eligible patients of a facility: everyone enrolled in HIV care plus HTS positives. */
    public Map<String, PatientSource> loadFacility(Long facilityId) {
        return load(facilityId, null);
    }

    /** One patient, or null when the patient does not belong to the facility. */
    public PatientSource loadPatient(Long facilityId, String personUuid) {
        return load(facilityId, personUuid).get(personUuid);
    }

    /** CBS-DQ-24: records in the facility that point at no active patient. */
    public List<CbsSourceRows.OrphanRecord> loadOrphanRecords(Long facilityId) {
        return clinical.orphanRecords(facilityId);
    }

    private Map<String, PatientSource> load(Long facilityId, String personUuid) {
        Map<String, PatientSource> patients = new LinkedHashMap<>();
        for (CbsSourceRows.Person row : clinical.persons(facilityId, personUuid)) {
            PatientSource s = new PatientSource();
            s.setPersonUuid(row.getUuid());
            s.setFacilityId(facilityId);
            s.setHospitalNumber(row.getHospitalNumber());
            s.setSex(row.getSex());
            s.setDateOfBirth(date(row.getDateOfBirth()));
            s.setDateOfBirthEstimated(Boolean.TRUE.equals(row.getDobEstimated()));
            patients.put(s.getPersonUuid(), s);
        }
        if (patients.isEmpty()) {
            return patients;
        }

        // Rows come earliest first, so the first enrolment per person is kept
        for (CbsSourceRows.Enrollment row : clinical.enrollments(facilityId, personUuid)) {
            PatientSource s = patients.get(row.getPersonUuid());
            if (s != null && s.getEnrollment() == null) {
                s.setEnrollment(new PatientSource.Enrollment(row.getUuid(), row.getUniqueId(), date(row.getDateConfirmedHiv()),
                        date(row.getDateOfRegistration()), date(row.getDateStarted()), row.getStatusAtRegistration(),
                        dateTime(row.getCreatedDate())));
            }
        }
        for (CbsSourceRows.ClinicVisit row : clinical.clinicVisits(facilityId, personUuid)) {
            PatientSource s = patients.get(row.getPersonUuid());
            if (s == null) {
                continue;
            }
            // Old commencement rows here are duplicates of what's now in hiv_enrollment_commencement
            if (Boolean.TRUE.equals(row.getIsCommencement())) {
                continue;
            }
            s.getClinicVisits().add(new PatientSource.DatedRecord(row.getUuid(), date(row.getVisitDate()), dateTime(row.getCreatedDate())));
        }
        for (CbsSourceRows.Commencement row : clinical.commencements(facilityId, personUuid)) {
            PatientSource s = patients.get(row.getPersonUuid());
            if (s != null) {
                s.getCommencements().add(new PatientSource.Commencement(row.getUuid(), date(row.getRecordDate()),
                        dateTime(row.getCreatedDate()), row.getRegimenId(), row.getRegimenLineId(),
                        blankToNull(row.getCd4AtStart()), row.getClinicalStage()));
            }
        }
        for (CbsSourceRows.HtsResult row : clinical.htsResults(facilityId, personUuid)) {
            PatientSource s = patients.get(row.getPersonUuid());
            if (s != null) {
                PatientSource.DatedRecord r = new PatientSource.DatedRecord(row.getUuid(), date(row.getRecordDate()), dateTime(row.getCreatedDate()));
                (("Positive".equalsIgnoreCase(row.getResult())) ? s.getHtsPositives() : s.getHtsNegatives()).add(r);
            }
        }
        for (CbsSourceRows.Refill row : clinical.refills(facilityId, personUuid)) {
            PatientSource s = patients.get(row.getPersonUuid());
            if (s != null) {
                s.getRefills().add(new PatientSource.Refill(row.getUuid(), date(row.getVisitDate()), row.getRefillPeriod(),
                        row.getRegimenLine(), dateTime(row.getCreatedDate())));
            }
        }
        for (CbsSourceRows.TrackerStatus row : clinical.trackerStatuses(facilityId, personUuid)) {
            PatientSource s = patients.get(row.getPersonUuid());
            if (s != null) {
                s.getTrackerStatuses().add(new PatientSource.TrackerStatus(row.getUuid(), row.getHivStatus(),
                        CbsTrackerStatus.normalise(row.getHivStatus()), date(row.getStatusDate()), blankToNull(row.getCauseOfDeath())));
            }
        }
        for (CbsSourceRows.PatientTracking row : clinical.patientTrackings(facilityId, personUuid)) {
            PatientSource s = patients.get(row.getPersonUuid());
            if (s != null) {
                s.getTrackings().add(new PatientSource.PatientTracking(row.getUuid(), date(row.getTrackingDate()),
                        date(row.getDateOfDeath()), blankToNull(row.getCauseOfDeath()), date(row.getDateReturned())));
            }
        }
        for (CbsSourceRows.DatedRecord row : clinical.transferInForms(facilityId, personUuid)) {
            PatientSource s = patients.get(row.getPersonUuid());
            if (s != null) {
                s.getTransferInForms().add(new PatientSource.DatedRecord(row.getUuid(), date(row.getRecordDate()), dateTime(row.getCreatedDate())));
            }
        }
        for (CbsSourceRows.LabTest row : clinical.labTests(facilityId, personUuid)) {
            PatientSource s = patients.get(row.getPersonUuid());
            if (s == null) {
                continue;
            }
            PatientSource.LabTest t = new PatientSource.LabTest(row.getUuid(), date(row.getSampleDate()), blankToNull(row.getSampleNumber()),
                    row.getIndication(), date(row.getResultDate()), row.getResultReported(), parseCopies(row.getResultReported()));
            (row.getLabTestId() != null && row.getLabTestId() == LAB_TEST_VIRAL_LOAD ? s.getViralLoads() : s.getCd4Tests()).add(t);
        }
        for (CbsSourceRows.DatedRecord row : clinical.eacSessions(facilityId, personUuid)) {
            PatientSource s = patients.get(row.getPersonUuid());
            if (s != null) {
                s.getEacSessions().add(new PatientSource.DatedRecord(row.getUuid(), date(row.getRecordDate()), dateTime(row.getCreatedDate())));
            }
        }
        for (CbsSourceRows.DatedRecord row : clinical.completedEacs(facilityId, personUuid)) {
            PatientSource s = patients.get(row.getPersonUuid());
            if (s != null && row.getRecordDate() != null) {
                s.getCompletedEacs().add(new PatientSource.DatedRecord(row.getUuid(), date(row.getRecordDate()), dateTime(row.getCreatedDate())));
            }
        }
        for (CbsSourceRows.DuplicateId row : clinical.duplicateIds(facilityId, personUuid)) {
            PatientSource s = patients.get(row.getPersonUuid());
            if (s != null) {
                s.getDuplicateIds().add(new PatientSource.DuplicateId(row.getIdType(), row.getIdValue(),
                        row.getOtherPatients() == null ? 1 : row.getOtherPatients()));
            }
        }
        return patients;
    }

    /**
     * Lab staff type results in many ways: "40", " 30", "< 20Copies/mL", "NotDetected", "failed".
     * Undetectable results count as 0. Anything else without a number is left as null, which
     * the engine treats as "can't be read" rather than suppressed.
     */
    static Double parseCopies(String raw) {
        if (raw == null) {
            return null;
        }
        String s = raw.trim().toLowerCase();
        if (s.isEmpty()) {
            return null;
        }
        if (s.contains("not detected") || s.contains("notdetected") || s.contains("undetect") || s.startsWith("<") || s.equals("tnd")) {
            return 0d;
        }
        Matcher m = NUMBER.matcher(s.replace(",", ""));
        return m.find() ? Double.valueOf(m.group(1)) : null;
    }

    // Native queries hand back java.sql.Date / Timestamp; java.sql.Date has no toInstant()
    private static LocalDate date(Date d) {
        if (d == null) {
            return null;
        }
        if (d instanceof java.sql.Date) {
            return ((java.sql.Date) d).toLocalDate();
        }
        return Instant.ofEpochMilli(d.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private static LocalDateTime dateTime(Date d) {
        return d == null ? null : Instant.ofEpochMilli(d.getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    private static String blankToNull(String s) {
        return s == null || s.trim().isEmpty() ? null : s.trim();
    }
}
