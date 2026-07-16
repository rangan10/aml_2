package com.insurance.aml.service;

import com.insurance.aml.dto.RuleEvaluationResult;
import com.insurance.aml.entity.AmlAlert;
import com.insurance.aml.entity.Customer;
import com.insurance.aml.entity.Policy;
import com.insurance.aml.repository.PolicyRepository;
import com.insurance.aml.ruleengine.RuleContext;
import com.insurance.aml.ruleengine.RuleEngineExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Ties the {@link RuleEngineExecutor} together with alert persistence.
 * Evaluates every rule for a given policy (which also carries its customer)
 * and creates alerts for anything that triggers.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RuleEvaluationService {

    private final RuleEngineExecutor ruleEngineExecutor;
    private final AmlAlertService amlAlertService;
    private final PolicyRepository policyRepository;

    /**
     * Evaluates all rules for a single policy and persists any resulting alerts.
     * Returns the list of alerts actually created (excludes duplicates that
     * were suppressed because an open alert for the same rule already exists).
     */
    public List<AmlAlert> evaluatePolicy(Long policyId) {
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new IllegalArgumentException("Policy not found: " + policyId));

        Customer customer = policy.getCustomer();
        RuleContext context = RuleContext.builder().customer(customer).policy(policy).build();

        List<RuleEvaluationResult> triggered = ruleEngineExecutor.evaluateAll(context);

        return triggered.stream()
                .map(result -> amlAlertService.createAlertIfNotDuplicate(customer, policy, result))
                .filter(alert -> alert != null)
                .toList();
    }

    /**
     * Evaluates all rules for every active policy in the system. Intended to
     * be invoked by the daily scheduler; also exposed via a manual trigger endpoint.
     */
    public int evaluateAllPolicies() {
        List<Policy> policies = policyRepository.findAll();
        int alertsCreated = 0;

        for (Policy policy : policies) {
            try {
                alertsCreated += evaluatePolicy(policy.getPolicyId()).size();
            } catch (Exception ex) {
                log.error("Failed to evaluate rules for policy {}: {}", policy.getPolicyId(), ex.getMessage(), ex);
            }
        }

        log.info("Rule engine run complete: {} policies evaluated, {} new alerts created",
                policies.size(), alertsCreated);
        return alertsCreated;
    }
}
