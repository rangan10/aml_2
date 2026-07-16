package com.insurance.aml.ruleengine;

import com.insurance.aml.config.AmlRuleProperties;
import com.insurance.aml.dto.RuleEvaluationResult;
import com.insurance.aml.entity.AlertSeverity;
import com.insurance.aml.entity.PaymentMode;
import com.insurance.aml.entity.PaymentTransaction;
import com.insurance.aml.entity.Policy;
import com.insurance.aml.repository.PaymentTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * AML004
 * - Cash/DD payment &gt;= 50,000 (single transaction)
 * - Monthly count &gt; 3 OR monthly amount &gt; 5 Lakhs (cash/DD, rolling 30 days)
 * - Yearly count &gt; 21 OR yearly amount &gt; 50 Lakhs (cash/DD, rolling 365 days)
 */
@Component
@RequiredArgsConstructor
public class Aml004Rule implements AmlRule {

    private static final String RULE_CODE = "AML004";
    private static final List<PaymentMode> CASH_DD_MODES = List.of(PaymentMode.CASH, PaymentMode.DD);

    private final AmlRuleProperties properties;
    private final PaymentTransactionRepository paymentTransactionRepository;

    @Override
    public String getRuleCode() {
        return RULE_CODE;
    }

    @Override
    public RuleEvaluationResult evaluate(RuleContext context) {
        Policy policy = context.getPolicy();
        if (policy == null || policy.getCustomer() == null) {
            return RuleEvaluationResult.notTriggered();
        }

        Long customerId = policy.getCustomer().getCustomerId();
        AmlRuleProperties.Aml004 cfg = properties.getAml004();

        LocalDateTime now = LocalDateTime.now();
        List<PaymentTransaction> lastMonth = paymentTransactionRepository
                .findByCustomerAndModesBetween(customerId, CASH_DD_MODES, now.minusDays(30), now);
        List<PaymentTransaction> lastYear = paymentTransactionRepository
                .findByCustomerAndModesBetween(customerId, CASH_DD_MODES, now.minusDays(365), now);

        StringBuilder reasons = new StringBuilder();
        boolean triggered = false;

        boolean singleTxnBreach = lastMonth.stream()
                .anyMatch(t -> t.getAmount() != null && t.getAmount().compareTo(cfg.getCashDdThreshold()) >= 0);
        if (singleTxnBreach) {
            triggered = true;
            reasons.append("Single cash/DD payment >= ").append(cfg.getCashDdThreshold()).append(". ");
        }

        BigDecimal monthlyAmount = sum(lastMonth);
        if (lastMonth.size() > cfg.getMonthlyCountThreshold() || monthlyAmount.compareTo(cfg.getMonthlyAmountThreshold()) > 0) {
            triggered = true;
            reasons.append("Monthly cash/DD count=").append(lastMonth.size())
                    .append(", amount=").append(monthlyAmount).append(" exceeds threshold. ");
        }

        BigDecimal yearlyAmount = sum(lastYear);
        if (lastYear.size() > cfg.getYearlyCountThreshold() || yearlyAmount.compareTo(cfg.getYearlyAmountThreshold()) > 0) {
            triggered = true;
            reasons.append("Yearly cash/DD count=").append(lastYear.size())
                    .append(", amount=").append(yearlyAmount).append(" exceeds threshold. ");
        }

        if (!triggered) {
            return RuleEvaluationResult.notTriggered();
        }

        return RuleEvaluationResult.builder()
                .triggered(true)
                .ruleCode(RULE_CODE)
                .severity(AlertSeverity.HIGH)
                .description("AML004 triggered: " + reasons.toString().trim())
                .build();
    }

    private BigDecimal sum(List<PaymentTransaction> transactions) {
        return transactions.stream()
                .map(PaymentTransaction::getAmount)
                .filter(a -> a != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
