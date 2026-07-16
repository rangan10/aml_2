package com.insurance.aml.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class CreateQuestionnaireRequest {

    @NotBlank
    private String questionnaireCode;

    @NotBlank
    private String name;

    private String description;

    @NotNull
    private LocalDate effectiveFrom;

    private LocalDate effectiveTo;

    @Valid
    private List<AddQuestionRequest> questions;
}
