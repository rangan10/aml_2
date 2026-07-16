package com.insurance.aml.ruleengine;

import com.insurance.aml.entity.Customer;
import com.insurance.aml.entity.Policy;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Input context passed to every AmlRule evaluation.
 * A rule may be evaluated at the customer level, the policy level, or both,
 * depending on which fields are populated.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RuleContext {

    /** Customer being evaluated. Always populated. */
    private Customer customer;

    /** Policy being evaluated, if the rule is policy-scoped. May be null for pure customer-level checks. */
    private Policy policy;
}
