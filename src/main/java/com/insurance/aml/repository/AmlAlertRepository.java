package com.insurance.aml.repository;

import com.insurance.aml.enums.AlertStatus;
import com.insurance.aml.entity.AmlAlert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AmlAlertRepository extends JpaRepository<AmlAlert, Long> {

    List<AmlAlert> findByCustomer_CustomerId(Long customerId);

    List<AmlAlert> findByStatus(AlertStatus status);

    List<AmlAlert> findByRuleCode(String ruleCode);

    boolean existsByCustomer_CustomerIdAndPolicy_PolicyIdAndRuleCodeAndStatusNot(
            Long customerId, Long policyId, String ruleCode, AlertStatus status);
}
