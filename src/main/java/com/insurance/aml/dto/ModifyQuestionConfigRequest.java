package com.insurance.aml.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
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
public class ModifyQuestionConfigRequest {

    @NotNull
    private Boolean mandatory;

    @NotNull
    @PositiveOrZero
    private Integer displayOrder;

    @Valid
    private ConditionalRuleDto conditionalRule;
}
