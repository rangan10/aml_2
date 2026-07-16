package com.insurance.aml.repository;

import com.insurance.aml.entity.DocumentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentStatusRepository extends JpaRepository<DocumentStatus, Long> {

    List<DocumentStatus> findByPolicy_PolicyId(Long policyId);

    boolean existsByPolicy_PolicyIdAndMissingFlagTrue(Long policyId);
}
