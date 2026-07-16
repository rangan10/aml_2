package com.insurance.aml.ruleengine;

import com.insurance.aml.config.AmlRuleProperties;
import com.insurance.aml.dto.RuleEvaluationResult;
import com.insurance.aml.entity.AlertSeverity;
import com.insurance.aml.entity.Policy;
import com.insurance.aml.entity.RefundTransaction;
import com.insurance.aml.repository.RefundTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * AML006
 * - Premium &gt;= 50,000 AND Refund &gt;= 50,000 (same policy)
 */
@Component
@RequiredArgsConstructor
public class Aml006Rule implements AmlRule {

    private static final String RULE_CODE = "AML006";

    private final AmlRuleProperties properties;
    private final RefundTransactionRepository refundTransactionRepository;

    @Override
    public String getRuleCode() {
        return RULE_CODE;
    }

    @Override
    public RuleEvaluationResult evaluate(RuleContext context) {
        Policy policy = context.getPolicy();
        if (policy == null || policy.getAnnualPremium() == null) {
            return RuleEvaluationResult.notTriggered();
        }

        AmlRuleProperties.Aml006 cfg = properties.getAml006();
        if (policy.getAnnualPremium().compareTo(cfg.getPremiumThreshold()) < 0) {
            return RuleEvaluationResult.notTriggered();
        }

        List<RefundTransaction> refunds = refundTransactionRepository.findByPolicy_PolicyId(policy.getPolicyId());
        BigDecimal totalRefund = refunds.stream()
                .map(RefundTransaction::getRefundAmount)
                .filter(a -> a != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalRefund.compareTo(cfg.getRefundThreshold()) < 0) {
            return RuleEvaluationResult.notTriggered();
        }

        return RuleEvaluationResult.builder()
                .triggered(true)
                .ruleCode(RULE_CODE)
                .severity(AlertSeverity.HIGH)
                .description("AML006 triggered: premium=" + policy.getAnnualPremium()
                        + " and total refund=" + totalRefund + " both exceed threshold "
                        + cfg.getPremiumThreshold() + ".")
                .build();
    }
}
