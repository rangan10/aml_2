package com.insurance.aml.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A question is only shown/mandatory if the answer to {@code dependsOnQuestionCode}
 * satisfies {@code operator} against {@code expectedValue} (comma-separated for IN).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConditionalRuleDto {

    @NotBlank
    private String dependsOnQuestionCode;

    @NotNull
    private ConditionalOperator operator;

    @NotBlank
    private String expectedValue;
}
