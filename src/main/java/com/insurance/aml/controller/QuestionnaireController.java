package com.insurance.aml.controller;

import com.insurance.aml.dto.AddQuestionRequest;
import com.insurance.aml.dto.CreateQuestionnaireRequest;
import com.insurance.aml.dto.ModifyQuestionConfigRequest;
import com.insurance.aml.dto.QuestionnaireDto;
import com.insurance.aml.service.QuestionnaireService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tenants/{tenantId}/questionnaires")
@RequiredArgsConstructor
public class QuestionnaireController {

    private final QuestionnaireService questionnaireService;

    @PostMapping
    public ResponseEntity<QuestionnaireDto> createQuestionnaire(@PathVariable Long tenantId,
                                                                 @Valid @RequestBody CreateQuestionnaireRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(questionnaireService.createQuestionnaire(tenantId, request));
    }

    @GetMapping
    public ResponseEntity<List<QuestionnaireDto>> getQuestionnaires(@PathVariable Long tenantId) {
        return ResponseEntity.ok(questionnaireService.getQuestionnaires(tenantId));
    }

    @GetMapping("/{questionnaireId}")
    public ResponseEntity<QuestionnaireDto> getQuestionnaire(@PathVariable Long tenantId,
                                                              @PathVariable Long questionnaireId) {
        return ResponseEntity.ok(questionnaireService.getQuestionnaire(tenantId, questionnaireId));
    }

    @PostMapping("/{questionnaireId}/assignments")
    public ResponseEntity<QuestionnaireDto> assignQuestionnaire(@PathVariable Long tenantId,
                                                                 @PathVariable Long questionnaireId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(questionnaireService.assignQuestionnaire(tenantId, questionnaireId));
    }

    @PostMapping("/{questionnaireId}/questions")
    public ResponseEntity<QuestionnaireDto> addQuestion(@PathVariable Long tenantId,
                                                         @PathVariable Long questionnaireId,
                                                         @Valid @RequestBody AddQuestionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(questionnaireService.addQuestion(tenantId, questionnaireId, request));
    }

    @PutMapping("/{questionnaireId}/questions/{questionId}")
    public ResponseEntity<QuestionnaireDto> modifyQuestionConfig(@PathVariable Long tenantId,
                                                                  @PathVariable Long questionnaireId,
                                                                  @PathVariable Long questionId,
                                                                  @Valid @RequestBody ModifyQuestionConfigRequest request) {
        return ResponseEntity.ok(
                questionnaireService.modifyQuestionConfig(tenantId, questionnaireId, questionId, request));
    }

    @DeleteMapping("/{questionnaireId}/questions/{questionId}")
    public ResponseEntity<QuestionnaireDto> removeQuestion(@PathVariable Long tenantId,
                                                            @PathVariable Long questionnaireId,
                                                            @PathVariable Long questionId) {
        return ResponseEntity.ok(questionnaireService.removeQuestion(tenantId, questionnaireId, questionId));
    }
}
