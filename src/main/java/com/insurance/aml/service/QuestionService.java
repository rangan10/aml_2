package com.insurance.aml.service;

import com.insurance.aml.dto.QuestionDto;
import com.insurance.aml.dto.QuestionOptionDto;
import com.insurance.aml.entity.AmlQuestion;
import com.insurance.aml.enums.QuestionCategory;
import com.insurance.aml.repository.AmlQuestionOptionRepository;
import com.insurance.aml.repository.AmlQuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Browses the question bank available to a tenant: global questions plus the
 * tenant's own tenant-specific questions, optionally filtered by category
 * (e.g. KYC, EMP, POLICY, QUOTATION) so callers can fetch just the questions
 * relevant to a given stage of the workflow.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuestionService {

    private final AmlQuestionRepository questionRepository;
    private final AmlQuestionOptionRepository questionOptionRepository;
    private final TenantService tenantService;

    public List<QuestionDto> getQuestions(Long tenantId, QuestionCategory category) {
        tenantService.findTenantOrThrow(tenantId);

        List<AmlQuestion> questions = new ArrayList<>();
        if (category != null) {
            questions.addAll(questionRepository.findByTenantIsNullAndCategory(category));
            questions.addAll(questionRepository.findByTenant_TenantIdAndCategory(tenantId, category));
        } else {
            questions.addAll(questionRepository.findByTenantIsNull());
            questions.addAll(questionRepository.findByTenant_TenantId(tenantId));
        }

        return questions.stream().map(this::toQuestionDto).toList();
    }

    private QuestionDto toQuestionDto(AmlQuestion question) {
        List<QuestionOptionDto> options = questionOptionRepository
                .findByQuestion_QuestionIdOrderByDisplayOrderAsc(question.getQuestionId())
                .stream()
                .map(o -> QuestionOptionDto.builder()
                        .optionId(o.getOptionId())
                        .optionCode(o.getOptionCode())
                        .optionLabel(o.getOptionLabel())
                        .displayOrder(o.getDisplayOrder())
                        .active(o.isActive())
                        .build())
                .toList();

        return QuestionDto.builder()
                .questionId(question.getQuestionId())
                .tenantId(question.getTenant() != null ? question.getTenant().getTenantId() : null)
                .questionCode(question.getQuestionCode())
                .questionText(question.getQuestionText())
                .questionType(question.getQuestionType())
                .questionScope(question.getQuestionScope())
                .category(question.getCategory())
                .active(question.isActive())
                .options(options)
                .build();
    }
}
