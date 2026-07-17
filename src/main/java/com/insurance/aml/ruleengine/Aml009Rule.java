package com.insurance.aml.ruleengine;

import com.insurance.aml.config.AmlRuleProperties;
import com.insurance.aml.dto.RuleEvaluationResult;
import com.insurance.aml.enums.AlertSeverity;
import com.insurance.aml.entity.Policy;
import com.insurance.aml.enums.RefundReason;
import com.insurance.aml.entity.RefundTransaction;
import com.insurance.aml.repository.RefundTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * AML009
 * - Overpayment refund &gt;= 10,000
 */
@Component
@RequiredArgsConstructor
public class Aml009Rule implements AmlRule {

    private static final String RULE_CODE = "AML009";

    private final AmlRuleProperties properties;
    private final RefundTransactionRepository refundTransactionRepository;

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

        AmlRuleProperties.Aml009 cfg = properties.getAml009();

        List<RefundTransaction> overpaymentRefunds = refundTransactionRepository
                .findByPolicy_PolicyId(policy.getPolicyId()).stream()
                .filter(r -> r.getRefundReason() == RefundReason.OVERPAYMENT)
                .toList();

        BigDecimal total = overpaymentRefunds.stream()
                .map(RefundTransaction::getRefundAmount)
                .filter(a -> a != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (total.compareTo(cfg.getOverpaymentRefundThreshold()) < 0) {
            return RuleEvaluationResult.notTriggered();
        }

        return RuleEvaluationResult.builder()
                .triggered(true)
                .ruleCode(RULE_CODE)
                .severity(AlertSeverity.MEDIUM)
                .description("AML009 triggered: overpayment refund total=" + total
                        + " exceeds threshold " + cfg.getOverpaymentRefundThreshold() + ".")
                .build();
    }
}
