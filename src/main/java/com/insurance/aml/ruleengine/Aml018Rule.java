package com.insurance.aml.ruleengine;

import com.insurance.aml.config.AmlRuleProperties;
import com.insurance.aml.dto.RuleEvaluationResult;
import com.insurance.aml.enums.AlertSeverity;
import com.insurance.aml.entity.PaymentTransaction;
import com.insurance.aml.entity.Policy;
import com.insurance.aml.repository.PaymentTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * AML018
 * - Premium &gt;= 2 Lakhs
 * - Payer != Insured
 * - Payer != Proposer
 * <p>
 * All three conditions must hold for any single payment on the policy.
 */
@Component
@RequiredArgsConstructor
public class Aml018Rule implements AmlRule {

    private static final String RULE_CODE = "AML018";

    private final AmlRuleProperties properties;
    private final PaymentTransactionRepository paymentTransactionRepository;

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

        var cfg = properties.getAml018();
        if (policy.getAnnualPremium().compareTo(cfg.getPremiumThreshold()) < 0) {
            return RuleEvaluationResult.notTriggered();
        }

        String insuredName = normalize(policy.getCustomer().getFullName());
        String proposerName = normalize(policy.getProposerName());

        List<PaymentTransaction> payments = paymentTransactionRepository
                .findByPolicy_PolicyId(policy.getPolicyId());

        boolean thirdPartyPayment = payments.stream().anyMatch(p -> {
            String payer = normalize(p.getPayerName());
            return !payer.isEmpty() && !payer.equals(insuredName) && !payer.equals(proposerName);
        });

        if (!thirdPartyPayment) {
            return RuleEvaluationResult.notTriggered();
        }

        return RuleEvaluationResult.builder()
                .triggered(true)
                .ruleCode(RULE_CODE)
                .severity(AlertSeverity.HIGH)
                .description("AML018 triggered: premium >= " + cfg.getPremiumThreshold()
                        + " paid by a party who is neither the insured nor the proposer.")
                .build();
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }
}
