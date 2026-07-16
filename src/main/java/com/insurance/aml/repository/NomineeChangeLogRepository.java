package com.insurance.aml.repository;

import com.insurance.aml.entity.NomineeChangeLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NomineeChangeLogRepository extends JpaRepository<NomineeChangeLog, Long> {

    long countByPolicy_PolicyId(Long policyId);
}
