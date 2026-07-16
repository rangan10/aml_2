package com.insurance.aml.dto;

import com.insurance.aml.entity.AlertSeverity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Result produced by a single AmlRule evaluation against a RuleContext.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RuleEvaluationResult {

    private boolean triggered;
    private String ruleCode;
    private String description;
    private AlertSeverity severity;

    public static RuleEvaluationResult notTriggered() {
        return RuleEvaluationResult.builder().triggered(false).build();
    }
}
