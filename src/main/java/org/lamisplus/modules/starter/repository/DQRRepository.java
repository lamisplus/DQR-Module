package org.lamisplus.modules.starter.repository;

import org.lamisplus.modules.starter.domain.entity.DQR;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DQRRepository extends JpaRepository<DQR, Long> {
}
