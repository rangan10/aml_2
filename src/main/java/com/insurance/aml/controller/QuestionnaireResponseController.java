//package com.insurance.aml.controller;
//
//import com.insurance.aml.dto.QuestionResponseDto;
//import com.insurance.aml.dto.SubmitQuestionResponseRequest;
//import com.insurance.aml.service.QuestionnaireResponseService;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/v1/tenants/{tenantId}/questionnaire-response")
//@RequiredArgsConstructor
//public class QuestionnaireResponseController {
//
//    private final QuestionnaireResponseService questionnaireResponseService;
//
//    @PostMapping
//    public ResponseEntity<QuestionResponseDto> submitResponse(
//            @PathVariable Long tenantId,
//            @Valid @RequestBody SubmitQuestionResponseRequest request) {
//        return ResponseEntity.status(HttpStatus.CREATED)
//                .body(questionnaireResponseService.submitResponse(tenantId, request));
//    }
//
//    @GetMapping("/{customerId}")
//    public ResponseEntity<List<QuestionResponseDto>> getResponsesForCustomer(@PathVariable Long tenantId,
//                                                                             @PathVariable Long customerId) {
//        return ResponseEntity.ok(questionnaireResponseService.getResponsesForCustomer(tenantId, customerId));
//    }
//}
