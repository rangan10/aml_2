package com.insurance.aml.dto;

import com.insurance.aml.enums.QuestionCategory;
import com.insurance.aml.enums.QuestionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Attaches a question to a questionnaire. Set {@code existingQuestionCode} to
 * reuse a question already in the bank (global, or owned by this tenant).
 * Leave it null and populate {@code questionCode}/{@code questionText}/
 * {@code questionType}/{@code category} to create a brand new tenant-specific
 * question inline.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddQuestionRequest {

    private String existingQuestionCode;

    private String questionCode;

    private String questionText;

    private QuestionType questionType;

    private QuestionCategory category;

    private List<QuestionOptionDto> options;

    @NotNull
    private Boolean mandatory;

    @NotNull
    @PositiveOrZero
    private Integer displayOrder;

    @Valid
    private ConditionalRuleDto conditionalRule;
}
