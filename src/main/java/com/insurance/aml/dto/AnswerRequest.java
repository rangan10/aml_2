package com.insurance.aml.dto;

import jakarta.validation.constraints.NotBlank;
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
public class AnswerRequest {

    @NotBlank
    private String questionCode;

    /** Raw value for TEXT/NUMBER/DATE/BOOLEAN questions. */
    private String answerText;

    /** Selected option code(s) for SINGLE_CHOICE/MULTI_CHOICE questions. */
    private List<String> selectedOptionCodes;
}
