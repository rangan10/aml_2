package com.insurance.aml.ruleengine;

import com.insurance.aml.config.AmlRuleProperties;
import com.insurance.aml.dto.RuleEvaluationResult;
import com.insurance.aml.entity.*;
import com.insurance.aml.enums.OccupationType;
import com.insurance.aml.enums.RefundReason;
import com.insurance.aml.repository.RefundTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class Aml006RuleTest {

    @Mock
    private RefundTransactionRepository refundTransactionRepository;

    private Aml006Rule rule;
    private Policy policy;

    @BeforeEach
    void setUp() {
        AmlRuleProperties properties = new AmlRuleProperties();
        // defaults: premiumThreshold=50000, refundThreshold=50000
        rule = new Aml006Rule(properties, refundTransactionRepository);

        Customer customer = Customer.builder()
                .customerId(1L)
                .customerCode("CUST-300")
                .fullName("Alice Smith")
                .panNumber("ABCDE1234F")
                .mobileNumber("9876543210")
                .email("alice@example.com")
                .addressCurrent("Addr")
                .addressPermanent("Addr")
                .annualIncome(BigDecimal.valueOf(2000000))
                .occupationType(OccupationType.SALARIED)
                .build();

        policy = Policy.builder()
                .policyId(20L)
                .policyNumber("POL-300")
                .customer(customer)
                .productType("ULIP")
                .proposerName("Alice Smith")
                .sumInsured(BigDecimal.valueOf(2000000))
                .annualPremium(BigDecimal.valueOf(60000))
                .assetHypothecated(false)
                .policyStartDate(LocalDate.now().minusMonths(6))
                .build();
    }

    @Test
    void premiumAndRefundBothAboveThreshold_triggersRule() {
        RefundTransaction refund = RefundTransaction.builder()
                .policy(policy)
                .refundAmount(BigDecimal.valueOf(55000))
                .refundDate(LocalDate.now())
                .refundReason(RefundReason.POLICY_CANCELLATION)
                .build();

        when(refundTransactionRepository.findByPolicy_PolicyId(20L)).thenReturn(List.of(refund));

        RuleEvaluationResult result = rule.evaluate(RuleContext.builder().policy(policy).build());

        assertThat(result.isTriggered()).isTrue();
        assertThat(result.getRuleCode()).isEqualTo("AML006");
    }

    @Test
    void refundBelowThreshold_doesNotTrigger() {
        RefundTransaction refund = RefundTransaction.builder()
                .policy(policy)
                .refundAmount(BigDecimal.valueOf(10000))
                .refundDate(LocalDate.now())
                .refundReason(RefundReason.OTHER)
                .build();

        when(refundTransactionRepository.findByPolicy_PolicyId(20L)).thenReturn(List.of(refund));

        RuleEvaluationResult result = rule.evaluate(RuleContext.builder().policy(policy).build());

        assertThat(result.isTriggered()).isFalse();
    }

    @Test
    void premiumBelowThreshold_neverQueriesRefunds() {
        policy.setAnnualPremium(BigDecimal.valueOf(10000));

        RuleEvaluationResult result = rule.evaluate(RuleContext.builder().policy(policy).build());

        assertThat(result.isTriggered()).isFalse();
    }

    @Test
    void missingPolicy_doesNotTrigger() {
        RuleEvaluationResult result = rule.evaluate(RuleContext.builder().policy(null).build());
        assertThat(result.isTriggered()).isFalse();
    }
}
