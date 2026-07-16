package com.insurance.aml.controller;

import com.insurance.aml.dto.AlertActionRequest;
import com.insurance.aml.dto.AmlAlertDto;
import com.insurance.aml.entity.AlertStatus;
import com.insurance.aml.service.AlertWorkflowService;
import com.insurance.aml.service.AmlAlertService;
import com.insurance.aml.service.RuleEvaluationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/aml-alerts")
@RequiredArgsConstructor
public class AmlAlertController {

    private final AmlAlertService amlAlertService;
    private final AlertWorkflowService alertWorkflowService;
    private final RuleEvaluationService ruleEvaluationService;

    @GetMapping("/{alertId}")
    public ResponseEntity<AmlAlertDto> getAlert(@PathVariable Long alertId) {
        return ResponseEntity.ok(amlAlertService.getAlert(alertId));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<AmlAlertDto>> getAlertsForCustomer(@PathVariable Long customerId) {
        return ResponseEntity.ok(amlAlertService.getAlertsForCustomer(customerId));
    }

    @GetMapping
    public ResponseEntity<List<AmlAlertDto>> getAlertsByStatus(
            @RequestParam(required = false, defaultValue = "OPEN") AlertStatus status) {
        return ResponseEntity.ok(amlAlertService.getAlertsByStatus(status));
    }

    /**
     * Advances an alert through the workflow:
     * System -> Operations Team -> Operations HOD -> Compliance Officer -> FIU / Close
     */
    @PostMapping("/{alertId}/actions")
    public ResponseEntity<AmlAlertDto> applyAction(@PathVariable Long alertId,
                                                    @Valid @RequestBody AlertActionRequest request) {
        return ResponseEntity.ok(alertWorkflowService.applyAction(alertId, request));
    }

    /**
     * Manually triggers rule evaluation for a single policy (in addition to
     * the nightly scheduled run). Useful for on-demand re-checks.
     */
    @PostMapping("/evaluate/policy/{policyId}")
    public ResponseEntity<Map<String, Object>> evaluatePolicy(@PathVariable Long policyId) {
        var alerts = ruleEvaluationService.evaluatePolicy(policyId);
        return ResponseEntity.ok(Map.of("policyId", policyId, "alertsCreated", alerts.size()));
    }

    /**
     * Manually triggers rule evaluation across all policies. Same operation
     * the scheduler runs nightly.
     */
    @PostMapping("/evaluate/all")
    public ResponseEntity<Map<String, Object>> evaluateAll() {
        int alertsCreated = ruleEvaluationService.evaluateAllPolicies();
        return ResponseEntity.ok(Map.of("alertsCreated", alertsCreated));
    }
}
