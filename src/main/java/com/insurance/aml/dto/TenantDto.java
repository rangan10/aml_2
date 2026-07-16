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
public class TenantDto {

    private Long tenantId;

    @NotBlank
    private String tenantCode;

    @NotBlank
    private String tenantName;

    private boolean active;
}
