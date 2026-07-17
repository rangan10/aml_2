package com.insurance.aml.ruleengine;

import com.insurance.aml.config.AmlRuleProperties;
import com.insurance.aml.dto.RuleEvaluationResult;
import com.insurance.aml.enums.AlertSeverity;
import com.insurance.aml.entity.ClaimEvent;
import com.insurance.aml.entity.Policy;
import com.insurance.aml.repository.ClaimEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * AML016
 * - Non-hypothecated asset
 * - First year claim (claim date within 365 days of policy start)
 * - Claim &gt;= 1 Crore
 */
@Component
@RequiredArgsConstructor
public class Aml016Rule implements AmlRule {

    private static final String RULE_CODE = "AML016";

    private final AmlRuleProperties properties;
    private final ClaimEventRepository claimEventRepository;

    @Override
    public String getRuleCode() {
        return RULE_CODE;
    }

    @Override
    public RuleEvaluationResult evaluate(RuleContext context) {
        Policy policy = context.getPolicy();
        if (policy == null || policy.isAssetHypothecated() || policy.getPolicyStartDate() == null) {
            return RuleEvaluationResult.notTriggered();
        }

        List<ClaimEvent> claims = claimEventRepository.findByPolicy_PolicyId(policy.getPolicyId());
        var threshold = properties.getAml016().getClaimThreshold();

        boolean matched = claims.stream().anyMatch(c ->
                c.getClaimDate() != null
                        && !c.getClaimDate().isAfter(policy.getPolicyStartDate().plusDays(365))
                        && c.getClaimAmount() != null
                        && c.getClaimAmount().compareTo(threshold) >= 0);

        if (!matched) {
            return RuleEvaluationResult.notTriggered();
        }

        return RuleEvaluationResult.builder()
                .triggered(true)
                .ruleCode(RULE_CODE)
                .severity(AlertSeverity.CRITICAL)
                .description("AML016 triggered: first-year claim >= " + threshold
                        + " on a non-hypothecated asset policy.")
                .build();
    }
}
