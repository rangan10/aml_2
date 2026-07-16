package com.insurance.aml.repository;

import com.insurance.aml.entity.AssigneeChangeLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssigneeChangeLogRepository extends JpaRepository<AssigneeChangeLog, Long> {

    long countByPolicy_PolicyId(Long policyId);
}
