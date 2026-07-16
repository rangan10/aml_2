package com.insurance.aml.scheduler;

import com.insurance.aml.service.RuleEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Runs the full AML rule engine across every policy in the system on a
 * schedule (default: 2 AM daily, configurable via aml.scheduler.cron).
 * Manual/on-demand evaluation is also available via
 * {@code POST /api/v1/aml-alerts/evaluate/all} and
 * {@code POST /api/v1/aml-alerts/evaluate/policy/{policyId}} for ad-hoc runs.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AmlRuleScheduler {

    private final RuleEvaluationService ruleEvaluationService;

    @Scheduled(cron = "${aml.scheduler.cron:0 0 2 * * *}")
    public void runDailyRuleEvaluation() {
        log.info("Starting scheduled AML rule engine run");
        long start = System.currentTimeMillis();
        int alertsCreated = ruleEvaluationService.evaluateAllPolicies();
        long durationMs = System.currentTimeMillis() - start;
        log.info("Scheduled AML rule engine run complete in {} ms; {} new alerts created",
                durationMs, alertsCreated);
    }
}
