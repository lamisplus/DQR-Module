package org.lamisplus.modules.dqr.repository;

import org.lamisplus.modules.dqr.domain.ClinicalSummaryDTOProjection;
import org.lamisplus.modules.dqr.domain.entity.DQA;
import org.lamisplus.modules.dqr.util.DQRQuerie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface ClinicalSummaryRepository extends JpaRepository<DQA, Long> {

    @Query(value = DQRQuerie.ClinicalVariables.CLINICAL_VARIABLE_SUMMARY_QUERIES, nativeQuery = true)
    List<ClinicalSummaryDTOProjection> getClinicalSummary (Long facilityId);



}
