package com.insurance.aml.dto;

import com.insurance.aml.enums.QuestionnaireStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionnaireDto {

    private Long questionnaireId;

    private Long tenantId;

    private String questionnaireCode;

    private String name;

    private String description;

    private int version;

    private QuestionnaireStatus status;

    private LocalDate effectiveFrom;

    private LocalDate effectiveTo;

    private Long previousVersionId;

    private List<QuestionnaireQuestionDto> questions;
}
