package com.insurance.aml.service;

import com.insurance.aml.dto.*;
import com.insurance.aml.entity.*;
import com.insurance.aml.enums.*;
import com.insurance.aml.exception.InvalidQuestionnaireStateException;
import com.insurance.aml.exception.ResourceNotFoundException;
import com.insurance.aml.repository.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Browses the question bank available to a tenant: global questions plus the
 * tenant's own tenant-specific questions, optionally filtered by category
 * (e.g. KYC, EMP, POLICY, QUOTATION, PROFILE, DOCUMENT, NOMINEE, PAYMENT) so
 * callers can fetch just the questions relevant to a given stage of the
 * workflow.
 */
@Service
@RequiredArgsConstructor
public class QuestionService {

    private final AmlQuestionRepository questionRepository;
    private final AmlQuestionOptionRepository questionOptionRepository;
    private final AmlQuestionTenantRepo amlQuestionTenantRepo;
    private final AmlQuestionResponseRepository responseRepository;
    private final AmlQuestionOptionResponseRepo optionResponseRepo;

    public List<QuestionDto> getQuestions(Long tenantId, QuestionCategory category) {
//        tenantService.findTenantOrThrow(tenantId);

        List<AmlQuestion> questions = new ArrayList<>();
        if (category != null) {
            questions.addAll(questionRepository.findByQuestionScopeAndCategory(QuestionScope.GLOBAL,category));
            questions.addAll(questionRepository.findQuestionsByTenantAndCategory(tenantId, category));
        } else {
            questions.addAll(questionRepository.findByQuestionScope(QuestionScope.GLOBAL));
            questions.addAll(questionRepository.findByTenantId(tenantId));
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
//                .tenantId(question.getTenant() != null ? question.getTenant().getTenantId() : null)
                .questionCode(question.getQuestionCode())
                .questionText(question.getQuestionText())
                .questionType(question.getQuestionType())
                .questionScope(question.getQuestionScope())
                .category(question.getCategory())
                .active(question.isActive())
                .options(options)
                .build();
    }

    @Transactional
    public String submitResponse(Long tenantId, @Valid SubmitQuestionResponseRequest request) {

//        Tenant tenant = tenantService.findTenantOrThrow(tenantId);
//
//        Customer customer = customerRepository.findById(request.getCustomerId())
//                .orElseThrow(() -> ResourceNotFoundException.forEntity("Customer", request.getCustomerId()));

        for(AnswerRequest answer : request.getAnswers()) {
            saveQuestionResponse(tenantId, request.getUserProfileId(), answer);
        }

        return "your response has been submitted successfully";
    }

    public void saveQuestionResponse(Long tenantId, Long userProfileId, AnswerRequest answer) {

        System.out.println(answer.getQuestionId()+", "+tenantId);
        AmlQuestion question = amlQuestionTenantRepo
                .findByTenantIdAndQuestionId(tenantId,
                        answer.getQuestionId())
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Question", answer.getQuestionId()));



        AmlQuestionResponse questionResponse = AmlQuestionResponse.builder()
                .tenantId(tenantId)
                .userProfileId(userProfileId)
                .question(question)
                .options(new ArrayList<>())
                .status(QuestionnaireResponseStatus.SUBMITTED)
                .submittedAt(LocalDateTime.now())
                .build();

        System.out.println("question.getQuestionType(): "+question.getQuestionType());

        if (question.getQuestionType() == QuestionType.SINGLE_CHOICE
                || question.getQuestionType() == QuestionType.MULTI_CHOICE) {

            List<Long> optionIds = answer.getSelectedOptionCodes() != null
                    ? answer.getSelectedOptionCodes() : List.of();

            for (Long optionId : optionIds) {

                AmlQuestionOption option = questionOptionRepository
                        .findByQuestion_QuestionIdAndOptionId(
                                question.getQuestionId(), optionId)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Unknown option Id '" + optionId + "' for question "
                                        + question.getQuestionCode()));
                AmlQuestionOptionResponse optionResponse =
                        AmlQuestionOptionResponse.builder()
                                .response(questionResponse)
                                .option(option)
                                .build();

                questionResponse.getOptions().add(optionResponse);
            }
        } else {
            questionResponse.setAnswerText(answer.getAnswerText());
        }
        responseRepository.save(questionResponse);
    }

    @Transactional
    public AmlUserQuestionResponseDto getResponsesForCustomer(Long tenantId, Long userProfileId) {


        List<AmlQuestionResponse> response = responseRepository.findLatestResponsesByUserAndTenant(userProfileId, tenantId);

        List<QuestionAnswerDto> answers = response.stream()
                .map(r -> QuestionAnswerDto.builder()
                        .responseId(r.getQuestionResponseId())
                        .questionId(r.getQuestion().getQuestionId())
                        .questionText(r.getQuestion().getQuestionText())
                        .answerText(r.getAnswerText())
                        .selectedOptionId(r.getOptions().stream()
                                .map(ro -> ro.getOption().getOptionId())
                                .toList())
                        .build())
                .toList();

        return AmlUserQuestionResponseDto.builder()
                .answers(answers)
                .userProfileId(userProfileId)
                .tenant(tenantId)
                .build();
    }
}