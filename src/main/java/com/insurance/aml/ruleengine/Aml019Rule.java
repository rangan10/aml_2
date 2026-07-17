package com.insurance.aml.ruleengine;

import com.insurance.aml.config.AmlRuleProperties;
import com.insurance.aml.dto.RuleEvaluationResult;
import com.insurance.aml.enums.AlertSeverity;
import com.insurance.aml.entity.Policy;
import com.insurance.aml.repository.MCAScreeningResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * AML019
 * - MCA (Ministry of Corporate Affairs) screening match
 * - Premium &gt;= 5 Lakhs
 * Both conditions must hold together.
 */
@Component
@RequiredArgsConstructor
public class Aml019Rule implements AmlRule {

    private static final String RULE_CODE = "AML019";

    private final AmlRuleProperties properties;
    private final MCAScreeningResultRepository mcaScreeningResultRepository;

    @Override
    public String getRuleCode() {
        return RULE_CODE;
    }

    @Override
    public RuleEvaluationResult evaluate(RuleContext context) {
        Policy policy = context.getPolicy();
        if (policy == null || policy.getAnnualPremium() == null || policy.getCustomer() == null) {
            return RuleEvaluationResult.notTriggered();
        }

        var cfg = properties.getAml019();
        if (policy.getAnnualPremium().compareTo(cfg.getPremiumThreshold()) < 0) {
            return RuleEvaluationResult.notTriggered();
        }

        boolean mcaMatch = !mcaScreeningResultRepository
                .findByCustomer_CustomerIdAndMatchFoundTrue(policy.getCustomer().getCustomerId())
                .isEmpty();

        if (!mcaMatch) {
            return RuleEvaluationResult.notTriggered();
        }

        return RuleEvaluationResult.builder()
                .triggered(true)
                .ruleCode(RULE_CODE)
                .severity(AlertSeverity.CRITICAL)
                .description("AML019 triggered: MCA screening match found and premium >= "
                        + cfg.getPremiumThreshold() + ".")
                .build();
    }
}
