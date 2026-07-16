package com.insurance.aml.ruleengine;

import com.insurance.aml.config.AmlRuleProperties;
import com.insurance.aml.dto.RuleEvaluationResult;
import com.insurance.aml.entity.*;
import com.insurance.aml.repository.PaymentTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class Aml004RuleTest {

    @Mock
    private PaymentTransactionRepository paymentTransactionRepository;

    private Aml004Rule rule;
    private Customer customer;
    private Policy policy;

    @BeforeEach
    void setUp() {
        AmlRuleProperties properties = new AmlRuleProperties();
        // defaults: cashDdThreshold=50000, monthlyCount=3, monthlyAmount=500000,
        //           yearlyCount=21, yearlyAmount=5000000
        rule = new Aml004Rule(properties, paymentTransactionRepository);

        customer = Customer.builder()
                .customerId(1L)
                .customerCode("CUST-200")
                .fullName("John Payer")
                .panNumber("ABCDE1234F")
                .mobileNumber("9876543210")
                .email("john@example.com")
                .addressCurrent("Addr")
                .addressPermanent("Addr")
                .annualIncome(BigDecimal.valueOf(1000000))
                .occupationType(OccupationType.BUSINESS)
                .build();

        policy = Policy.builder()
                .policyId(10L)
                .policyNumber("POL-200")
                .customer(customer)
                .productType("HEALTH")
                .proposerName("John Payer")
                .sumInsured(BigDecimal.valueOf(1000000))
                .annualPremium(BigDecimal.valueOf(50000))
                .assetHypothecated(false)
                .policyStartDate(LocalDate.now().minusYears(1))
                .build();
    }

    @Test
    void singleCashPaymentAtOrAboveThreshold_triggersRule() {
        PaymentTransaction txn = paymentOf(BigDecimal.valueOf(50000), PaymentMode.CASH);

        when(paymentTransactionRepository.findByCustomerAndModesBetween(eq(1L), anyList(), any(), any()))
                .thenReturn(List.of(txn));

        RuleEvaluationResult result = rule.evaluate(RuleContext.builder().customer(customer).policy(policy).build());

        assertThat(result.isTriggered()).isTrue();
        assertThat(result.getRuleCode()).isEqualTo("AML004");
    }

    @Test
    void monthlyCountExceedsThreshold_triggersRule() {
        List<PaymentTransaction> monthly = List.of(
                paymentOf(BigDecimal.valueOf(10000), PaymentMode.CASH),
                paymentOf(BigDecimal.valueOf(10000), PaymentMode.CASH),
                paymentOf(BigDecimal.valueOf(10000), PaymentMode.DD),
                paymentOf(BigDecimal.valueOf(10000), PaymentMode.DD)
        );

        // first call = last 30 days, second call = last 365 days
        when(paymentTransactionRepository.findByCustomerAndModesBetween(eq(1L), anyList(), any(), any()))
                .thenReturn(monthly)
                .thenReturn(monthly);

        RuleEvaluationResult result = rule.evaluate(RuleContext.builder().customer(customer).policy(policy).build());

        assertThat(result.isTriggered()).isTrue();
        assertThat(result.getDescription()).contains("Monthly");
    }

    @Test
    void noBreaches_doesNotTrigger() {
        when(paymentTransactionRepository.findByCustomerAndModesBetween(eq(1L), anyList(), any(), any()))
                .thenReturn(List.of());

        RuleEvaluationResult result = rule.evaluate(RuleContext.builder().customer(customer).policy(policy).build());

        assertThat(result.isTriggered()).isFalse();
    }

    @Test
    void missingPolicy_doesNotTrigger() {
        RuleEvaluationResult result = rule.evaluate(RuleContext.builder().customer(customer).policy(null).build());
        assertThat(result.isTriggered()).isFalse();
    }

    private PaymentTransaction paymentOf(BigDecimal amount, PaymentMode mode) {
        return PaymentTransaction.builder()
                .policy(policy)
                .paymentMode(mode)
                .amount(amount)
                .paymentDate(LocalDateTime.now())
                .payerName("John Payer")
                .build();
    }
}
