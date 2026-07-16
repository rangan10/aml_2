package com.insurance.aml.controller;

import com.insurance.aml.dto.QuestionnaireResponseDto;
import com.insurance.aml.dto.SubmitQuestionnaireResponseRequest;
import com.insurance.aml.service.QuestionnaireResponseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tenants/{tenantId}/questionnaire-response")
@RequiredArgsConstructor
public class QuestionnaireResponseController {

    private final QuestionnaireResponseService questionnaireResponseService;

    @PostMapping
    public ResponseEntity<QuestionnaireResponseDto> submitResponse(
            @PathVariable Long tenantId,
            @Valid @RequestBody SubmitQuestionnaireResponseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(questionnaireResponseService.submitResponse(tenantId, request));
    }

    @GetMapping("/{customerId}")
    public ResponseEntity<List<QuestionnaireResponseDto>> getResponsesForCustomer(@PathVariable Long tenantId,
                                                                                   @PathVariable Long customerId) {
        return ResponseEntity.ok(questionnaireResponseService.getResponsesForCustomer(tenantId, customerId));
    }
}
