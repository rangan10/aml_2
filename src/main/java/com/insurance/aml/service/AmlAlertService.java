package com.insurance.aml.service;

import com.insurance.aml.dto.AmlAlertDto;
import com.insurance.aml.dto.RuleEvaluationResult;
import com.insurance.aml.entity.*;
import com.insurance.aml.exception.ResourceNotFoundException;
import com.insurance.aml.repository.AmlAlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Responsible for turning triggered rule evaluation results into persisted
 * AmlAlert records, and for basic alert read operations. Workflow stage
 * transitions live in {@link AlertWorkflowService}.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AmlAlertService {

    private final AmlAlertRepository amlAlertRepository;

    /**
     * Persists a new alert for the given customer/policy if an open alert
     * for the same rule does not already exist (avoids duplicate noise on
     * every scheduler run while the underlying condition remains true).
     */
    public AmlAlert createAlertIfNotDuplicate(Customer customer, Policy policy, RuleEvaluationResult result) {
        Long policyId = policy != null ? policy.getPolicyId() : null;

        boolean alreadyOpen = amlAlertRepository.existsByCustomer_CustomerIdAndPolicy_PolicyIdAndRuleCodeAndStatusNot(
                customer.getCustomerId(), policyId, result.getRuleCode(), AlertStatus.CLOSED);

        if (alreadyOpen) {
            log.debug("Skipping duplicate alert for rule {} on customer {}", result.getRuleCode(),
                    customer.getCustomerId());
            return null;
        }

        AmlAlert alert = AmlAlert.builder()
                .customer(customer)
                .policy(policy)
                .ruleCode(result.getRuleCode())
                .description(result.getDescription())
                .severity(result.getSeverity())
                .status(AlertStatus.OPEN)
                .currentStage(AlertStage.SYSTEM)
                .build();

        AmlAlert saved = amlAlertRepository.save(alert);
        log.info("Created AML alert id={} rule={} customer={} policy={}",
                saved.getAlertId(), saved.getRuleCode(), customer.getCustomerId(), policyId);
        return saved;
    }

    @Transactional(readOnly = true)
    public AmlAlertDto getAlert(Long alertId) {
        return toDto(findAlertOrThrow(alertId));
    }

    @Transactional(readOnly = true)
    public List<AmlAlertDto> getAlertsForCustomer(Long customerId) {
        return amlAlertRepository.findByCustomer_CustomerId(customerId).stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<AmlAlertDto> getAlertsByStatus(AlertStatus status) {
        return amlAlertRepository.findByStatus(status).stream().map(this::toDto).toList();
    }

    AmlAlert findAlertOrThrow(Long alertId) {
        return amlAlertRepository.findById(alertId)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("AmlAlert", alertId));
    }

    AmlAlertDto toDto(AmlAlert alert) {
        return AmlAlertDto.builder()
                .alertId(alert.getAlertId())
                .customerId(alert.getCustomer().getCustomerId())
                .policyId(alert.getPolicy() != null ? alert.getPolicy().getPolicyId() : null)
                .ruleCode(alert.getRuleCode())
                .description(alert.getDescription())
                .severity(alert.getSeverity())
                .status(alert.getStatus())
                .currentStage(alert.getCurrentStage())
                .createdAt(alert.getCreatedAt())
                .updatedAt(alert.getUpdatedAt())
                .build();
    }
}
