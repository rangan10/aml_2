//package com.insurance.aml.service;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.insurance.aml.dto.AnswerRequest;
//import com.insurance.aml.dto.ConditionalRuleDto;
//import com.insurance.aml.dto.QuestionAnswerDto;
//import com.insurance.aml.dto.QuestionResponseDto;
//import com.insurance.aml.dto.SubmitQuestionResponseRequest;
//import com.insurance.aml.entity.AmlQuestion;
//import com.insurance.aml.entity.AmlQuestionOption;
//import com.insurance.aml.entity.AmlQuestionResponse;
//import com.insurance.aml.entity.AmlQuestionnaireResponse;
//import com.insurance.aml.entity.AmlQuestionnaireTenant;
//import com.insurance.aml.entity.AmlTenantQuestionnaire;
//import com.insurance.aml.entity.Customer;
//import com.insurance.aml.entity.Policy;
//import com.insurance.aml.enums.QuestionType;
//import com.insurance.aml.enums.QuestionnaireResponseStatus;
//import com.insurance.aml.enums.QuestionnaireStatus;
//import com.insurance.aml.entity.Tenant;
//import com.insurance.aml.exception.InvalidQuestionnaireStateException;
//import com.insurance.aml.exception.MandatoryQuestionMissingException;
//import com.insurance.aml.exception.ResourceNotFoundException;
//import com.insurance.aml.repository.AmlQuestionOptionRepository;
//import com.insurance.aml.repository.AmlQuestionResponseRepository;
//import com.insurance.aml.repository.AmlQuestionnaireResponseRepository;
//import com.insurance.aml.repository.AmlQuestionnaireTenantRepository;
//import com.insurance.aml.repository.AmlTenantQuestionnaireRepository;
//import com.insurance.aml.repository.CustomerRepository;
//import com.insurance.aml.repository.PolicyRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.LinkedHashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Objects;
//import java.util.stream.Collectors;
//
///**
// * Validates and persists a customer's submission of a tenant's questionnaire,
// * and exposes past responses. Mandatory-question validation respects each
// * question's conditional display rule: a mandatory question that is not
// * currently applicable (its dependency isn't satisfied) is not required.
// */
//@Service
//@RequiredArgsConstructor
//@Transactional
//public class QuestionnaireResponseService {
//
//    private final AmlQuestionnaireResponseRepository responseRepository;
//    private final AmlQuestionResponseRepository questionResponseRepository;
//    private final AmlQuestionnaireTenantRepository questionnaireTenantRepository;
//    private final AmlTenantQuestionnaireRepository tenantQuestionnaireRepository;
//    private final AmlQuestionOptionRepository questionOptionRepository;
//    private final CustomerRepository customerRepository;
//    private final PolicyRepository policyRepository;
//    private final TenantService tenantService;
//    private final ObjectMapper objectMapper;
//
//    public QuestionResponseDto submitResponse(Long tenantId, SubmitQuestionResponseRequest request) {
//        Tenant tenant = tenantService.findTenantOrThrow(tenantId);
//
//        AmlQuestionnaireTenant instance = questionnaireTenantRepository
//                .findByQuestionnaire_QuestionnaireIdAndTenant_TenantIdAndStatus(
//                        request.getQuestionnaireId(), tenantId, QuestionnaireStatus.ACTIVE)
//                .orElseThrow(() -> ResourceNotFoundException.forEntity("Questionnaire", request.getQuestionnaireId()));
//
//        LocalDate today = LocalDate.now();
//        if (today.isBefore(instance.getEffectiveFrom())
//                || (instance.getEffectiveTo() != null && today.isAfter(instance.getEffectiveTo()))) {
//            throw new InvalidQuestionnaireStateException("Questionnaire "
//                    + instance.getQuestionnaire().getQuestionnaireCode() + " is not effective today");
//        }
//
//        Customer customer = customerRepository.findById(request.getCustomerId())
//                .orElseThrow(() -> ResourceNotFoundException.forEntity("Customer", request.getCustomerId()));
//
//        Policy policy = null;
////        if (request.getPolicyId() != null) {
////            policy = policyRepository.findById(request.getPolicyId())
////                    .orElseThrow(() -> ResourceNotFoundException.forEntity("Policy", request.getPolicyId()));
////        }
//
//        List<AmlTenantQuestionnaire> mappings = tenantQuestionnaireRepository
//                .findByQuestionnaireTenant_QuestionnaireTenantIdOrderByDisplayOrderAsc(
//                        instance.getQuestionnaireTenantId());
//
//        Map<String, AnswerRequest> answersByCode = request.getAnswers().stream()
//                .collect(Collectors.toMap(AnswerRequest::getQuestionCode, a -> a, (a, b) -> a));
//
//        validateMandatoryQuestions(mappings, answersByCode);
//
//        AmlQuestionnaireResponse response = AmlQuestionnaireResponse.builder()
//                .tenant(tenant)
//                .questionnaireTenant(instance)
//                .customer(customer)
//                .policy(policy)
//                .status(QuestionnaireResponseStatus.SUBMITTED)
//                .submittedAt(LocalDateTime.now())
//                .build();
//        response = responseRepository.save(response);
//
//        for (AmlTenantQuestionnaire mapping : mappings) {
//            AnswerRequest answer = answersByCode.get(mapping.getQuestion().getQuestionCode());
//            if (answer != null) {
//                saveQuestionResponse(tenant, response, mapping.getQuestion(), answer);
//            }
//        }
//
//        return toDto(reload(response.getResponseId()));
//    }
//
//    @Transactional(readOnly = true)
//    public List<QuestionResponseDto> getResponsesForCustomer(Long tenantId, Long customerId) {
//        tenantService.findTenantOrThrow(tenantId);
//        return responseRepository.findByTenant_TenantIdAndCustomer_CustomerId(tenantId, customerId)
//                .stream().map(this::toDto).toList();
//    }
//
//    private void validateMandatoryQuestions(List<AmlTenantQuestionnaire> mappings,
//                                             Map<String, AnswerRequest> answersByCode) {
//        List<String> missing = new ArrayList<>();
//        for (AmlTenantQuestionnaire mapping : mappings) {
//            AnswerRequest answer = answersByCode.get(mapping.getQuestion().getQuestionCode());
//            boolean unanswered = answer == null
//                    || ((answer.getAnswerText() == null || answer.getAnswerText().isBlank())
//                        && (answer.getSelectedOptionCodes() == null || answer.getSelectedOptionCodes().isEmpty()));
//            if (unanswered) {
//                missing.add(mapping.getQuestion().getQuestionCode());
//            }
//        }
//        if (!missing.isEmpty()) {
//            throw new MandatoryQuestionMissingException("Missing mandatory answers for: " + String.join(", ", missing));
//        }
//    }
//
////    private boolean isApplicable(AmlTenantQuestionnaire mapping, Map<String, AnswerRequest> answersByCode) {
////        ConditionalRuleDto rule = deserializeConditionalRule(mapping.getConditionalRule());
////        if (rule == null) {
////            return true;
////        }
////        AnswerRequest dependsOn = answersByCode.get(rule.getDependsOnQuestionCode());
////        String actual = dependsOn != null ? dependsOn.getAnswerText() : null;
////        return switch (rule.getOperator()) {
////            case EQUALS -> rule.getExpectedValue().equalsIgnoreCase(actual);
////            case NOT_EQUALS -> !rule.getExpectedValue().equalsIgnoreCase(actual);
////            case IN -> actual != null && Arrays.asList(rule.getExpectedValue().split(",")).contains(actual);
////        };
////    }
//
//    private void saveQuestionResponse(Tenant tenant, AmlQuestionnaireResponse response, AmlQuestion question,
//                                       AnswerRequest answer) {
//        if (question.getQuestionType() == QuestionType.SINGLE_CHOICE
//                || question.getQuestionType() == QuestionType.MULTI_CHOICE) {
//            List<Long> codes = answer.getSelectedOptionCodes() != null ? answer.getSelectedOptionCodes() : List.of();
//            for (Long code : codes) {
//                AmlQuestionOption option = questionOptionRepository
//                        .findByQuestion_QuestionIdAndOptionId(question.getQuestionId(), code)
//                        .orElseThrow(() -> new IllegalArgumentException(
//                                "Unknown option Id '" + code + "' for question " + question.getQuestionCode()));
//
//                questionResponseRepository.save(AmlQuestionResponse.builder()
//                        .tenant(tenant)
//                        .question(question)
//                        .option(option)
//                        .answerText(option.getOptionLabel())
//                        .build());
//            }
//        } else {
//            questionResponseRepository.save(AmlQuestionResponse.builder()
//                    .tenant(tenant)
//                    .question(question)
//                    .answerText(answer.getAnswerText())
//                    .build());
//        }
//    }
//
//    private AmlQuestionnaireResponse reload(Long responseId) {
//        return responseRepository.findById(responseId)
//                .orElseThrow(() -> ResourceNotFoundException.forEntity("QuestionnaireResponse", responseId));
//    }
//
//    private ConditionalRuleDto deserializeConditionalRule(String json) {
//        if (json == null) {
//            return null;
//        }
//        try {
//            return objectMapper.readValue(json, ConditionalRuleDto.class);
//        } catch (JsonProcessingException ex) {
//            throw new IllegalStateException("Corrupt conditional rule data", ex);
//        }
//    }
//
//    private QuestionResponseDto toDto(AmlQuestionnaireResponse response) {
//        List<AmlQuestionResponse> answers = questionResponseRepository.findByResponse_ResponseId(response.getResponseId());
//
//        Map<Long, List<AmlQuestionResponse>> byQuestion = answers.stream()
//                .collect(Collectors.groupingBy(a -> a.getQuestion().getQuestionId(), LinkedHashMap::new, Collectors.toList()));
//
//        List<QuestionAnswerDto> answerDtos = byQuestion.values().stream()
//                .map(this::toQuestionAnswerDto)
//                .toList();
//
//        AmlQuestionnaireTenant instance = response.getQuestionnaireTenant();
//        return QuestionResponseDto.builder()
//                .responseId(response.getResponseId())
//                .tenantId(response.getTenant().getTenantId())
//                .questionnaireId(instance.getQuestionnaire().getQuestionnaireId())
//                .questionnaireCode(instance.getQuestionnaire().getQuestionnaireCode())
//                .version(instance.getVersion())
//                .customerId(response.getCustomer().getCustomerId())
//                .policyId(response.getPolicy() != null ? response.getPolicy().getPolicyId() : null)
//                .status(response.getStatus())
//                .submittedAt(response.getSubmittedAt())
//                .answers(answerDtos)
//                .build();
//    }
//
//    private QuestionAnswerDto toQuestionAnswerDto(List<AmlQuestionResponse> rows) {
//        AmlQuestionResponse first = rows.get(0);
//        List<String> optionCodes = rows.stream()
//                .map(AmlQuestionResponse::getOption)
//                .filter(Objects::nonNull)
//                .map(AmlQuestionOption::getOptionCode)
//                .toList();
//
//        return QuestionAnswerDto.builder()
//                .questionCode(first.getQuestion().getQuestionCode())
//                .questionText(first.getQuestion().getQuestionText())
//                .answerText(optionCodes.isEmpty() ? first.getAnswerText() : null)
//                .selectedOptionCodes(optionCodes.isEmpty() ? null : optionCodes)
//                .build();
//    }
//}
