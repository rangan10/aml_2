package com.insurance.aml.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmitQuestionnaireResponseRequest {

    @NotNull
    private Long questionnaireId;

    @NotNull
    private Long customerId;

    private Long policyId;

    @NotEmpty
    @Valid
    private List<AnswerRequest> answers;
}
