package com.insurance.aml.dto;

import com.insurance.aml.enums.QuestionCategory;
import com.insurance.aml.enums.QuestionScope;
import com.insurance.aml.enums.QuestionType;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Question bank detail, as it appears standalone or nested inside a
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionDto {

    private Long questionId;

    private Long tenantId;

    private String questionCode;

    private String questionText;

    private QuestionType questionType;

    private QuestionScope questionScope;

    private QuestionCategory category;

    private boolean active;

    private int displayOrder;

    private List<QuestionOptionDto> options;
}
