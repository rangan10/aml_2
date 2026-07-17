package com.insurance.aml.dto;

import com.insurance.aml.enums.QuestionnaireResponseStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionnaireResponseDto {

    private Long responseId;

    private Long tenantId;

    private Long questionnaireId;

    private String questionnaireCode;

    private int version;

    private Long customerId;

    private Long policyId;

    private QuestionnaireResponseStatus status;

    private LocalDateTime submittedAt;

    private List<QuestionAnswerDto> answers;
}
