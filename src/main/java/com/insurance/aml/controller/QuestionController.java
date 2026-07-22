package com.insurance.aml.controller;

import com.insurance.aml.dto.AmlUserQuestionResponseDto;
import com.insurance.aml.dto.QuestionDto;
import com.insurance.aml.dto.QuestionResponseDto;
import com.insurance.aml.dto.SubmitQuestionResponseRequest;
import com.insurance.aml.enums.QuestionCategory;
import com.insurance.aml.service.QuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tenants/{tenantId}/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    @GetMapping
    public ResponseEntity<List<QuestionDto>> getQuestions(
            @PathVariable Long tenantId,
            @RequestParam(name = "question_category", required = false) QuestionCategory questionCategory) {
        return ResponseEntity.ok(questionService.getQuestions(tenantId, questionCategory));
    }

    @PostMapping
    public ResponseEntity<String> submitResponse(
            @PathVariable Long tenantId,
            @Valid @RequestBody SubmitQuestionResponseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(questionService.submitResponse(tenantId, request));
    }

    @GetMapping("/{customerId}")
    public ResponseEntity<AmlUserQuestionResponseDto> getResponsesForCustomer(@PathVariable Long tenantId,
                                                                              @PathVariable Long customerId) {
        return ResponseEntity.ok(questionService.getResponsesForCustomer(tenantId, customerId));
    }
}
