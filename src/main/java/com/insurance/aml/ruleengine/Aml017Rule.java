package com.insurance.aml.ruleengine;

import com.insurance.aml.config.AmlRuleProperties;
import com.insurance.aml.dto.RuleEvaluationResult;
import com.insurance.aml.enums.AlertSeverity;
import com.insurance.aml.entity.Policy;
import com.insurance.aml.repository.AssigneeChangeLogRepository;
import com.insurance.aml.repository.DocumentStatusRepository;
import com.insurance.aml.repository.NomineeChangeLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * AML017
 * - Missing documents
 * - Nominee changes &gt; 2
 * - Assignee changes &gt; 2
 * <p>
 * Any single condition triggers the rule.
 */
@Component
@RequiredArgsConstructor
public class Aml017Rule implements AmlRule {

    private static final String RULE_CODE = "AML017";

    private final AmlRuleProperties properties;
    private final DocumentStatusRepository documentStatusRepository;
    private final NomineeChangeLogRepository nomineeChangeLogRepository;
    private final AssigneeChangeLogRepository assigneeChangeLogRepository;

    @Override
    public String getRuleCode() {
        return RULE_CODE;
    }

    @Override
    public RuleEvaluationResult evaluate(RuleContext context) {
        Policy policy = context.getPolicy();
        if (policy == null) {
            return RuleEvaluationResult.notTriggered();
        }

        AmlRuleProperties.Aml017 cfg = properties.getAml017();
        StringBuilder reasons = new StringBuilder();
        boolean triggered = false;

        boolean missingDocs = documentStatusRepository
                .existsByPolicy_PolicyIdAndMissingFlagTrue(policy.getPolicyId());
        if (missingDocs) {
            triggered = true;
            reasons.append("Missing documents on file. ");
        }

        long nomineeChanges = nomineeChangeLogRepository.countByPolicy_PolicyId(policy.getPolicyId());
        if (nomineeChanges > cfg.getNomineeChangeThreshold()) {
            triggered = true;
            reasons.append("Nominee changes=").append(nomineeChanges)
                    .append(" exceeds threshold ").append(cfg.getNomineeChangeThreshold()).append(". ");
        }

        long assigneeChanges = assigneeChangeLogRepository.countByPolicy_PolicyId(policy.getPolicyId());
        if (assigneeChanges > cfg.getAssigneeChangeThreshold()) {
            triggered = true;
            reasons.append("Assignee changes=").append(assigneeChanges)
                    .append(" exceeds threshold ").append(cfg.getAssigneeChangeThreshold()).append(". ");
        }

        if (!triggered) {
            return RuleEvaluationResult.notTriggered();
        }

        return RuleEvaluationResult.builder()
                .triggered(true)
                .ruleCode(RULE_CODE)
                .severity(AlertSeverity.MEDIUM)
                .description("AML017 triggered: " + reasons.toString().trim())
                .build();
    }
}
