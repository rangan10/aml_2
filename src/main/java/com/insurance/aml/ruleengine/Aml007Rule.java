package com.insurance.aml.ruleengine;

import com.insurance.aml.config.AmlRuleProperties;
import com.insurance.aml.dto.RuleEvaluationResult;
import com.insurance.aml.entity.AlertSeverity;
import com.insurance.aml.entity.ClaimEvent;
import com.insurance.aml.entity.Policy;
import com.insurance.aml.repository.ClaimEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * AML007
 * - Claim logged
 * - Sum insured &gt;= 1 Crore
 * - Annual premium &gt;= 1 Lakh
 * All three conditions must hold together.
 */
@Component
@RequiredArgsConstructor
public class Aml007Rule implements AmlRule {

    private static final String RULE_CODE = "AML007";

    private final AmlRuleProperties properties;
    private final ClaimEventRepository claimEventRepository;

    @Override
    public String getRuleCode() {
        return RULE_CODE;
    }

    @Override
    public RuleEvaluationResult evaluate(RuleContext context) {
        Policy policy = context.getPolicy();
        if (policy == null || policy.getSumInsured() == null || policy.getAnnualPremium() == null) {
            return RuleEvaluationResult.notTriggered();
        }

        AmlRuleProperties.Aml007 cfg = properties.getAml007();

        boolean sumInsuredBreach = policy.getSumInsured().compareTo(cfg.getSumInsuredThreshold()) >= 0;
        boolean premiumBreach = policy.getAnnualPremium().compareTo(cfg.getPremiumThreshold()) >= 0;
        if (!sumInsuredBreach || !premiumBreach) {
            return RuleEvaluationResult.notTriggered();
        }

        List<ClaimEvent> claims = claimEventRepository.findByPolicy_PolicyId(policy.getPolicyId());
        if (claims.isEmpty()) {
            return RuleEvaluationResult.notTriggered();
        }

        return RuleEvaluationResult.builder()
                .triggered(true)
                .ruleCode(RULE_CODE)
                .severity(AlertSeverity.HIGH)
                .description("AML007 triggered: claim logged on policy with sum insured="
                        + policy.getSumInsured() + " (>=" + cfg.getSumInsuredThreshold()
                        + ") and annual premium=" + policy.getAnnualPremium()
                        + " (>=" + cfg.getPremiumThreshold() + ").")
                .build();
    }
}
