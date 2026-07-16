package com.insurance.aml.controller;

import com.insurance.aml.dto.PolicyDto;
import com.insurance.aml.service.PolicyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/policies")
@RequiredArgsConstructor
public class PolicyController {

    private final PolicyService policyService;

    @PostMapping
    public ResponseEntity<PolicyDto> createPolicy(@Valid @RequestBody PolicyDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(policyService.createPolicy(dto));
    }

    @GetMapping("/{policyId}")
    public ResponseEntity<PolicyDto> getPolicy(@PathVariable Long policyId) {
        return ResponseEntity.ok(policyService.getPolicy(policyId));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<PolicyDto>> getPoliciesForCustomer(@PathVariable Long customerId) {
        return ResponseEntity.ok(policyService.getPoliciesForCustomer(customerId));
    }
}
