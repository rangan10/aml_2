package com.insurance.aml.ruleengine;

import com.insurance.aml.config.AmlRuleProperties;
import com.insurance.aml.dto.RuleEvaluationResult;
import com.insurance.aml.enums.OccupationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class Aml003RuleTest {

    private Aml003Rule rule;

    @BeforeEach
    void setUp() {
        AmlRuleProperties properties = new AmlRuleProperties();
        // defaults: sumInsuredMultiplier=10, premiumMultiplier=5
        rule = new Aml003Rule(properties);
    }

    @Test
    void addressMismatch_triggersRule() {
        Customer customer = customerWith("Address A", "Address B", BigDecimal.valueOf(1000000));
        Policy policy = policyWith(BigDecimal.valueOf(5000000), BigDecimal.valueOf(100000));

        RuleEvaluationResult result = rule.evaluate(RuleContext.builder().customer(customer).policy(policy).build());

        assertThat(result.isTriggered()).isTrue();
        assertThat(result.getRuleCode()).isEqualTo("AML003");
        assertThat(result.getDescription()).contains("address");
    }

    @Test
    void sumInsuredExceedsTenTimesIncome_triggersRule() {
        Customer customer = customerWith("Same Address", "Same Address", BigDecimal.valueOf(100000));
        Policy policy = policyWith(BigDecimal.valueOf(1500000), BigDecimal.valueOf(100000));

        RuleEvaluationResult result = rule.evaluate(RuleContext.builder().customer(customer).policy(policy).build());

        assertThat(result.isTriggered()).isTrue();
        assertThat(result.getDescription()).contains("Sum insured");
    }

    @Test
    void premiumExceedsFiveTimesIncome_triggersRule() {
        Customer customer = customerWith("Same Address", "Same Address", BigDecimal.valueOf(100000));
        Policy policy = policyWith(BigDecimal.valueOf(500000), BigDecimal.valueOf(600000));

        RuleEvaluationResult result = rule.evaluate(RuleContext.builder().customer(customer).policy(policy).build());

        assertThat(result.isTriggered()).isTrue();
        assertThat(result.getDescription()).contains("Premium");
    }

    @Test
    void allConditionsWithinLimits_doesNotTrigger() {
        Customer customer = customerWith("Same Address", "Same Address", BigDecimal.valueOf(1000000));
        Policy policy = policyWith(BigDecimal.valueOf(5000000), BigDecimal.valueOf(300000));

        RuleEvaluationResult result = rule.evaluate(RuleContext.builder().customer(customer).policy(policy).build());

        assertThat(result.isTriggered()).isFalse();
    }

    @Test
    void missingPolicy_doesNotTrigger() {
        Customer customer = customerWith("A", "A", BigDecimal.valueOf(100000));
        RuleEvaluationResult result = rule.evaluate(RuleContext.builder().customer(customer).policy(null).build());
        assertThat(result.isTriggered()).isFalse();
    }

    private Customer customerWith(String currentAddress, String permanentAddress, BigDecimal income) {
        return Customer.builder()
                .customerCode("CUST-100")
                .fullName("Jane Doe")
                .panNumber("ABCDE1234F")
                .mobileNumber("9876543210")
                .email("jane@example.com")
                .addressCurrent(currentAddress)
                .addressPermanent(permanentAddress)
                .annualIncome(income)
                .occupationType(OccupationType.SALARIED)
                .build();
    }

    private Policy policyWith(BigDecimal sumInsured, BigDecimal premium) {
        return Policy.builder()
                .policyNumber("POL-100")
                .productType("TERM_LIFE")
                .proposerName("Jane Doe")
                .sumInsured(sumInsured)
                .annualPremium(premium)
                .assetHypothecated(false)
                .policyStartDate(LocalDate.now().minusDays(10))
                .build();
    }
}
