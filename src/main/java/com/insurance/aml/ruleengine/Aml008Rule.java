package com.insurance.aml.ruleengine;

import com.insurance.aml.config.AmlRuleProperties;
import com.insurance.aml.dto.RuleEvaluationResult;
import com.insurance.aml.entity.AlertSeverity;
import com.insurance.aml.entity.Customer;
import com.insurance.aml.repository.AddressChangeLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * AML008 - Address change monitoring
 * - 2nd change on the same customer  -> low severity notification
 * - 3rd (or subsequent) change       -> high severity alert
 * <p>
 * Both outcomes are surfaced as a RuleEvaluationResult; the workflow layer
 * decides whether a low severity "notification" result still needs to be
 * routed through the full alert workflow or simply logged.
 */
@Component
@RequiredArgsConstructor
public class Aml008Rule implements AmlRule {

    private static final String RULE_CODE = "AML008";

    private final AmlRuleProperties properties;
    private final AddressChangeLogRepository addressChangeLogRepository;

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

        AmlRuleProperties.Aml008 cfg = properties.getAml008();
        long changeCount = addressChangeLogRepository.countByCustomer_CustomerId(customer.getCustomerId());

        if (changeCount >= cfg.getAlertChangeCount()) {
            return RuleEvaluationResult.builder()
                    .triggered(true)
                    .ruleCode(RULE_CODE)
                    .severity(AlertSeverity.HIGH)
                    .description("AML008 triggered: customer has had " + changeCount
                            + " address changes (alert threshold=" + cfg.getAlertChangeCount() + ").")
                    .build();
        }

        if (changeCount >= cfg.getNotificationChangeCount()) {
            return RuleEvaluationResult.builder()
                    .triggered(true)
                    .ruleCode(RULE_CODE)
                    .severity(AlertSeverity.LOW)
                    .description("AML008 notification: customer has had " + changeCount
                            + " address changes (notification threshold=" + cfg.getNotificationChangeCount() + ").")
                    .build();
        }

        return RuleEvaluationResult.notTriggered();
    }
}
