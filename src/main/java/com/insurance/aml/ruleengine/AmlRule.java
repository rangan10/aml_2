package com.insurance.aml.ruleengine;

import com.insurance.aml.dto.RuleEvaluationResult;

/**
 * Contract implemented by every AML rule (AML003, AML004, ... AML019).
 * Each rule receives a RuleContext and returns a RuleEvaluationResult
 * indicating whether the rule was triggered and, if so, what alert to raise.
 */
public interface AmlRule {

    /** Unique rule code, e.g. "AML003". */
    String getRuleCode();

    /** Evaluates the rule against the given context. */
    RuleEvaluationResult evaluate(RuleContext context);
}
