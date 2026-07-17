package com.insurance.aml.controller;

import com.insurance.aml.dto.QuestionDto;
import com.insurance.aml.enums.QuestionCategory;
import com.insurance.aml.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tenants/{tenantId}/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    @GetMapping
    public ResponseEntity<List<QuestionDto>> getQuestions(@PathVariable Long tenantId,
                                                            @RequestParam(required = false) QuestionCategory category) {
        return ResponseEntity.ok(questionService.getQuestions(tenantId, category));
    }
}
