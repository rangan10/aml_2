package com.insurance.aml.dto;

import com.insurance.aml.entity.OccupationType;
import com.insurance.aml.entity.RiskCategory;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerDto {

    private Long customerId;

    @NotBlank
    private String customerCode;

    @NotBlank
    private String fullName;

    @NotBlank
    @Pattern(regexp = "^[A-Z]{5}[0-9]{4}[A-Z]{1}$", message = "Invalid PAN format")
    private String panNumber;

    @NotBlank
    @Pattern(regexp = "^[0-9]{10}$", message = "Mobile number must be 10 digits")
    private String mobileNumber;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String addressCurrent;

    @NotBlank
    private String addressPermanent;

    @NotNull
    @Positive
    private BigDecimal annualIncome;

    @NotNull
    private OccupationType occupationType;

    private boolean pep;

    private boolean nri;

    private RiskCategory riskCategory;
}
