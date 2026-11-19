package org.lamisplus.modules.dqr.repository.cbs;

import org.lamisplus.modules.dqr.domain.cbs.entity.CbsSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CbsSettingRepository extends JpaRepository<CbsSetting, String> {
    List<CbsSetting> findAllByOrderBySettingKey();
}
