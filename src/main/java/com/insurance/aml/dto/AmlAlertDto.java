package com.insurance.aml.dto;

import com.insurance.aml.enums.AlertSeverity;
import com.insurance.aml.enums.AlertStage;
import com.insurance.aml.enums.AlertStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AmlAlertDto {

    private Long alertId;
    private Long customerId;
    private Long policyId;
    private String ruleCode;
    private String description;
    private AlertSeverity severity;
    private AlertStatus status;
    private AlertStage currentStage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
