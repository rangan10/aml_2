package com.insurance.aml.ruleengine;

import com.insurance.aml.config.AmlRuleProperties;
import com.insurance.aml.dto.RuleEvaluationResult;
import com.insurance.aml.entity.AlertSeverity;
import com.insurance.aml.entity.Customer;
import com.insurance.aml.entity.Policy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * AML003
 * - Current Address != Permanent Address
 * - Sum Insured > Annual Income x 10
 * - Premium > Annual Income x 5
 * <p>
 * Any single condition triggers the rule; the description enumerates
 * which specific conditions matched.
 */
@Component
@RequiredArgsConstructor
public class Aml003Rule implements AmlRule {

    private static final String RULE_CODE = "AML003";

    private final AmlRuleProperties properties;

    @Override
    public String getRuleCode() {
        return RULE_CODE;
    }

    @Override
    public RuleEvaluationResult evaluate(RuleContext context) {
        Policy policy = context.getPolicy();
        Customer customer = context.getCustomer();
        if (policy == null || customer == null) {
            return RuleEvaluationResult.notTriggered();
        }

        StringBuilder reasons = new StringBuilder();
        boolean triggered = false;

        boolean addressMismatch = customer.getAddressCurrent() != null
                && !customer.getAddressCurrent().trim().equalsIgnoreCase(
                        customer.getAddressPermanent() == null ? "" : customer.getAddressPermanent().trim());
        if (addressMismatch) {
            triggered = true;
            reasons.append("Current address differs from permanent address. ");
        }

        BigDecimal income = customer.getAnnualIncome();
        int sumInsuredMultiplier = properties.getAml003().getSumInsuredMultiplier();
        int premiumMultiplier = properties.getAml003().getPremiumMultiplier();

        if (income != null && income.signum() > 0) {
            BigDecimal sumInsuredLimit = income.multiply(BigDecimal.valueOf(sumInsuredMultiplier));
            if (policy.getSumInsured() != null && policy.getSumInsured().compareTo(sumInsuredLimit) > 0) {
                triggered = true;
                reasons.append("Sum insured exceeds ").append(sumInsuredMultiplier)
                        .append("x annual income. ");
            }

            BigDecimal premiumLimit = income.multiply(BigDecimal.valueOf(premiumMultiplier));
            if (policy.getAnnualPremium() != null && policy.getAnnualPremium().compareTo(premiumLimit) > 0) {
                triggered = true;
                reasons.append("Premium exceeds ").append(premiumMultiplier)
                        .append("x annual income. ");
            }
        }

        if (!triggered) {
            return RuleEvaluationResult.notTriggered();
        }

        return RuleEvaluationResult.builder()
                .triggered(true)
                .ruleCode(RULE_CODE)
                .severity(AlertSeverity.HIGH)
                .description("AML003 triggered: " + reasons.toString().trim())
                .build();
    }
}
