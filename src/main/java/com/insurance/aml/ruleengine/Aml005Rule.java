package com.insurance.aml.ruleengine;

import com.insurance.aml.config.AmlRuleProperties;
import com.insurance.aml.dto.RuleEvaluationResult;
import com.insurance.aml.enums.AlertSeverity;
import com.insurance.aml.entity.Customer;
import com.insurance.aml.entity.FreeLookCancellation;
import com.insurance.aml.repository.FreeLookCancellationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * AML005
 * - Free look cancellation &gt;= 3 within 30 days (customer-level, across policies)
 */
@Component
@RequiredArgsConstructor
public class Aml005Rule implements AmlRule {

    private static final String RULE_CODE = "AML005";

    private final AmlRuleProperties properties;
    private final FreeLookCancellationRepository freeLookCancellationRepository;

    @Override
    public String getRuleCode() {
        return RULE_CODE;
    }

    @Override
    public RuleEvaluationResult evaluate(RuleContext context) {
        Customer customer = context.getCustomer();
        if (customer == null) {
            return RuleEvaluationResult.notTriggered();
        }

        AmlRuleProperties.Aml005 cfg = properties.getAml005();
        LocalDate now = LocalDate.now();

        List<FreeLookCancellation> recent = freeLookCancellationRepository
                .findByCustomerAndDateBetween(customer.getCustomerId(), now.minusDays(cfg.getWindowDays()), now);

        if (recent.size() < cfg.getCancellationCountThreshold()) {
            return RuleEvaluationResult.notTriggered();
        }

        return RuleEvaluationResult.builder()
                .triggered(true)
                .ruleCode(RULE_CODE)
                .severity(AlertSeverity.MEDIUM)
                .description("AML005 triggered: " + recent.size() + " free look cancellations within "
                        + cfg.getWindowDays() + " days (threshold=" + cfg.getCancellationCountThreshold() + ").")
                .build();
    }
}
