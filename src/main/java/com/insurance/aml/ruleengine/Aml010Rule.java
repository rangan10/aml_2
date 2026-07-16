package com.insurance.aml.ruleengine;

import com.insurance.aml.dto.RuleEvaluationResult;
import com.insurance.aml.entity.AlertSeverity;
import com.insurance.aml.entity.Customer;
import com.insurance.aml.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * AML010
 * - Duplicate PAN
 * - Duplicate Mobile
 * - Duplicate Email
 * - Duplicate Address
 * <p>
 * Flags when another customer record shares any one of these identity attributes.
 */
@Component
@RequiredArgsConstructor
public class Aml010Rule implements AmlRule {

    private static final String RULE_CODE = "AML010";

    private final CustomerRepository customerRepository;

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

        List<Customer> duplicates = customerRepository.findPotentialDuplicates(
                customer.getCustomerId(),
                customer.getPanNumber(),
                customer.getMobileNumber(),
                customer.getEmail(),
                customer.getAddressCurrent());

        if (duplicates.isEmpty()) {
            return RuleEvaluationResult.notTriggered();
        }

        return RuleEvaluationResult.builder()
                .triggered(true)
                .ruleCode(RULE_CODE)
                .severity(AlertSeverity.HIGH)
                .description("AML010 triggered: " + duplicates.size()
                        + " other customer record(s) share PAN/mobile/email/address with this customer.")
                .build();
    }
}
