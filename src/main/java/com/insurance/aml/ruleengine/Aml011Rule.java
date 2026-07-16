package com.insurance.aml.ruleengine;

import com.insurance.aml.dto.RuleEvaluationResult;
import com.insurance.aml.entity.AdverseMediaScreening;
import com.insurance.aml.entity.AlertSeverity;
import com.insurance.aml.entity.Customer;
import com.insurance.aml.repository.AdverseMediaScreeningRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * AML011 - Adverse Media Screening
 * Triggers when a positive (match_found = true) adverse media screening
 * record exists for the customer.
 */
@Component
@RequiredArgsConstructor
public class Aml011Rule implements AmlRule {

    private static final String RULE_CODE = "AML011";

    private final AdverseMediaScreeningRepository adverseMediaScreeningRepository;

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

        List<AdverseMediaScreening> matches = adverseMediaScreeningRepository
                .findByCustomer_CustomerIdAndMatchFoundTrue(customer.getCustomerId());

        if (matches.isEmpty()) {
            return RuleEvaluationResult.notTriggered();
        }

        String sources = matches.stream()
                .map(AdverseMediaScreening::getSource)
                .filter(s -> s != null && !s.isBlank())
                .distinct()
                .collect(Collectors.joining(", "));

        return RuleEvaluationResult.builder()
                .triggered(true)
                .ruleCode(RULE_CODE)
                .severity(AlertSeverity.CRITICAL)
                .description("AML011 triggered: adverse media match found"
                        + (sources.isBlank() ? "." : " (sources: " + sources + ")."))
                .build();
    }
}
