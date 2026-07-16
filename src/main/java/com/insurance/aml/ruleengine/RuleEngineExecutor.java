package com.insurance.aml.ruleengine;

import com.insurance.aml.dto.RuleEvaluationResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Runs every registered {@link AmlRule} bean against a given {@link RuleContext}
 * and collects the results of the rules that triggered. Spring injects every
 * AmlRule implementation automatically via constructor injection of the List.
 */
@Component
@Slf4j
public class RuleEngineExecutor {

    private final List<AmlRule> rules;

    public RuleEngineExecutor(List<AmlRule> rules) {
        this.rules = rules;
    }

    /**
     * Evaluates all rules against the given context and returns only the
     * results that were triggered.
     */
    public List<RuleEvaluationResult> evaluateAll(RuleContext context) {
        List<RuleEvaluationResult> triggered = new ArrayList<>();
        for (AmlRule rule : rules) {
            try {
                RuleEvaluationResult result = rule.evaluate(context);
                if (result != null && result.isTriggered()) {
                    triggered.add(result);
                }
            } catch (Exception ex) {
                log.error("Error evaluating rule {} for customerId={}, policyId={}: {}",
                        rule.getRuleCode(),
                        context.getCustomer() != null ? context.getCustomer().getCustomerId() : null,
                        context.getPolicy() != null ? context.getPolicy().getPolicyId() : null,
                        ex.getMessage(), ex);
            }
        }
        return triggered;
    }

    public int getRegisteredRuleCount() {
        return rules.size();
    }
}
