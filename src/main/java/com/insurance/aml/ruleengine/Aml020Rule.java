package com.insurance.aml.ruleengine;

import com.insurance.aml.dto.RuleEvaluationResult;
import com.insurance.aml.enums.AlertSeverity;
import org.springframework.stereotype.Component;

/**
 * AML020: flags customers who self-declared as a Politically Exposed Person
 * via the onboarding questionnaire, even when the core Customer record does
 * not (yet) carry the isPep flag. Demonstrates rule-engine integration with
 * dynamic, tenant-configured questionnaire responses.
 */
@Component
public class Aml020Rule implements AmlRule {

    private static final String RULE_CODE = "AML020";
    private static final String PEP_QUESTION_CODE = "PEP";

    @Override
    public String getRuleCode() {
        return RULE_CODE;
    }

    @Override
    public RuleEvaluationResult evaluate(RuleContext context) {
        String answer = context.getQuestionnaireAnswer(PEP_QUESTION_CODE);
        boolean declaredPep = "YES".equalsIgnoreCase(answer) || "TRUE".equalsIgnoreCase(answer);
        boolean alreadyFlagged = context.getCustomer() != null && context.getCustomer().isPep();

        if (!declaredPep || alreadyFlagged) {
            return RuleEvaluationResult.notTriggered();
        }

        return RuleEvaluationResult.builder()
                .triggered(true)
                .ruleCode(RULE_CODE)
                .description("Customer self-declared as a Politically Exposed Person via the onboarding questionnaire")
                .severity(AlertSeverity.HIGH)
                .build();
    }
}
