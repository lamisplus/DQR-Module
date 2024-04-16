package org.lamisplus.modules.dqr.repository;

import org.lamisplus.modules.dqr.domain.SummaryDTOProjection;
import org.lamisplus.modules.dqr.domain.entity.DQA;
import org.lamisplus.modules.dqr.util.DQRQuerie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PatientSummaryRepository extends JpaRepository<DQA, Long> {


    @Query(value = DQRQuerie.PatientDemographyQueries.DEMOGRAPHIC_SUMMARY_QUERY, nativeQuery = true)
    List <SummaryDTOProjection> patientDemoSummary (Long facilityId);
}
