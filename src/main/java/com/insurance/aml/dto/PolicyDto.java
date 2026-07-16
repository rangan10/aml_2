package com.insurance.aml.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PolicyDto {

    private Long policyId;

    @NotBlank
    private String policyNumber;

    @NotNull
    private Long customerId;

    @NotBlank
    private String productType;

    @NotBlank
    private String proposerName;

    @NotNull
    @Positive
    private BigDecimal sumInsured;

    @NotNull
    @Positive
    private BigDecimal annualPremium;

    private boolean assetHypothecated;

    @NotNull
    private LocalDate policyStartDate;
}
