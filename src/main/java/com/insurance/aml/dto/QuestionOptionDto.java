package com.insurance.aml.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionOptionDto {

    private Long optionId;

    @NotBlank
    private String optionCode;

    @NotBlank
    private String optionLabel;

    private int displayOrder;

    private boolean active;
}
