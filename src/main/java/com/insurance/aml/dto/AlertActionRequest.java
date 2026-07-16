package com.insurance.aml.dto;

import com.insurance.aml.entity.AlertAction;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class AlertActionRequest {

    @NotNull
    private AlertAction action;

    @NotBlank
    private String actionBy;

    private String remarks;
}
