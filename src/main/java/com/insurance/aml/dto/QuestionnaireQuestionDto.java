package com.insurance.aml.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** A question as configured within one specific questionnaire version. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionnaireQuestionDto {

    private Long tenantQuestionnaireId;

    private QuestionDto question;

    private boolean mandatory;

    private int displayOrder;

    private ConditionalRuleDto conditionalRule;
}
