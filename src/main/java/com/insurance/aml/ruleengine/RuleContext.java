package com.insurance.aml.ruleengine;

import com.insurance.aml.entity.Customer;
import com.insurance.aml.entity.Policy;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

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

    /**
     * Answers from the customer's latest submitted questionnaire response,
     * keyed by question code. Empty if no response has been submitted yet.
     */
    @Builder.Default
    private Map<String, String> questionnaireAnswers = Map.of();

    /** Convenience accessor for a single questionnaire answer by question code. */
    public String getQuestionnaireAnswer(String questionCode) {
        return questionnaireAnswers.get(questionCode);
    }
}
