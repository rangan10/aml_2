package com.insurance.aml.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insurance.aml.dto.AddQuestionRequest;
import com.insurance.aml.dto.ConditionalRuleDto;
import com.insurance.aml.dto.CreateQuestionnaireRequest;
import com.insurance.aml.dto.ModifyQuestionConfigRequest;
import com.insurance.aml.dto.QuestionDto;
import com.insurance.aml.dto.QuestionOptionDto;
import com.insurance.aml.dto.QuestionnaireDto;
import com.insurance.aml.dto.QuestionnaireQuestionDto;
import com.insurance.aml.entity.AmlQuestion;
import com.insurance.aml.entity.AmlQuestionOption;
import com.insurance.aml.entity.AmlQuestionnaire;
import com.insurance.aml.entity.AmlTenantQuestionnaire;
import com.insurance.aml.entity.QuestionnaireStatus;
import com.insurance.aml.entity.Tenant;
import com.insurance.aml.exception.DuplicateResourceException;
import com.insurance.aml.exception.ResourceNotFoundException;
import com.insurance.aml.repository.AmlQuestionOptionRepository;
import com.insurance.aml.repository.AmlQuestionRepository;
import com.insurance.aml.repository.AmlQuestionnaireRepository;
import com.insurance.aml.repository.AmlQuestionnaireResponseRepository;
import com.insurance.aml.repository.AmlTenantQuestionnaireRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Manages tenant-owned questionnaires: creation, attaching/removing/reconfiguring
 * questions, and the version-history mechanics. A structural change to an
 * existing questionnaire mutates it in place until customer responses have
 * been recorded against it; from that point on, structural changes create a
 * new version so historical responses keep referencing the exact version
 * they were answered against.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class QuestionnaireService {

    private final AmlQuestionnaireRepository questionnaireRepository;
    private final AmlQuestionRepository questionRepository;
    private final AmlQuestionOptionRepository questionOptionRepository;
    private final AmlTenantQuestionnaireRepository tenantQuestionnaireRepository;
    private final AmlQuestionnaireResponseRepository questionnaireResponseRepository;
    private final TenantService tenantService;
    private final ObjectMapper objectMapper;

    public QuestionnaireDto createQuestionnaire(Long tenantId, CreateQuestionnaireRequest request) {
        Tenant tenant = tenantService.findTenantOrThrow(tenantId);

        if (questionnaireRepository.findTopByTenant_TenantIdAndQuestionnaireCodeOrderByVersionDesc(
                tenantId, request.getQuestionnaireCode()).isPresent()) {
            throw DuplicateResourceException.forField(
                    "Questionnaire", "questionnaireCode", request.getQuestionnaireCode());
        }
        if (request.getEffectiveTo() != null && request.getEffectiveTo().isBefore(request.getEffectiveFrom())) {
            throw new IllegalArgumentException("effectiveTo must not be before effectiveFrom");
        }

        AmlQuestionnaire questionnaire = AmlQuestionnaire.builder()
                .tenant(tenant)
                .questionnaireCode(request.getQuestionnaireCode())
                .name(request.getName())
                .description(request.getDescription())
                .version(1)
                .status(QuestionnaireStatus.ACTIVE)
                .effectiveFrom(request.getEffectiveFrom())
                .effectiveTo(request.getEffectiveTo())
                .build();
        questionnaire = questionnaireRepository.save(questionnaire);

        if (request.getQuestions() != null) {
            for (AddQuestionRequest questionRequest : request.getQuestions()) {
                attachQuestion(tenant, questionnaire, questionRequest);
            }
        }

        return toDto(questionnaire);
    }

    public QuestionnaireDto addQuestion(Long tenantId, Long questionnaireId, AddQuestionRequest request) {
        Tenant tenant = tenantService.findTenantOrThrow(tenantId);
        AmlQuestionnaire current = findQuestionnaireOrThrow(tenantId, questionnaireId);

        AmlQuestionnaire target = prepareTargetVersion(tenant, current);
        attachQuestion(tenant, target, request);

        return toDto(reload(target.getQuestionnaireId()));
    }

    public QuestionnaireDto removeQuestion(Long tenantId, Long questionnaireId, Long questionId) {
        Tenant tenant = tenantService.findTenantOrThrow(tenantId);
        AmlQuestionnaire current = findQuestionnaireOrThrow(tenantId, questionnaireId);
        AmlQuestionnaire target = prepareTargetVersion(tenant, current);

        AmlTenantQuestionnaire mapping = tenantQuestionnaireRepository
                .findByQuestionnaire_QuestionnaireIdAndQuestion_QuestionId(target.getQuestionnaireId(), questionId)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("QuestionnaireQuestion", questionId));
        tenantQuestionnaireRepository.delete(mapping);

        return toDto(reload(target.getQuestionnaireId()));
    }

    public QuestionnaireDto modifyQuestionConfig(Long tenantId, Long questionnaireId, Long questionId,
                                                  ModifyQuestionConfigRequest request) {
        Tenant tenant = tenantService.findTenantOrThrow(tenantId);
        AmlQuestionnaire current = findQuestionnaireOrThrow(tenantId, questionnaireId);
        AmlQuestionnaire target = prepareTargetVersion(tenant, current);

        AmlTenantQuestionnaire mapping = tenantQuestionnaireRepository
                .findByQuestionnaire_QuestionnaireIdAndQuestion_QuestionId(target.getQuestionnaireId(), questionId)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("QuestionnaireQuestion", questionId));

        mapping.setMandatory(Boolean.TRUE.equals(request.getMandatory()));
        mapping.setDisplayOrder(request.getDisplayOrder());
        mapping.setConditionalRule(serializeConditionalRule(request.getConditionalRule()));
        tenantQuestionnaireRepository.save(mapping);

        return toDto(reload(target.getQuestionnaireId()));
    }

    @Transactional(readOnly = true)
    public List<QuestionnaireDto> getQuestionnaires(Long tenantId) {
        tenantService.findTenantOrThrow(tenantId);
        return questionnaireRepository.findByTenant_TenantIdAndStatusNot(tenantId, QuestionnaireStatus.INACTIVE)
                .stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public QuestionnaireDto getQuestionnaire(Long tenantId, Long questionnaireId) {
        return toDto(findQuestionnaireOrThrow(tenantId, questionnaireId));
    }

    AmlQuestionnaire findQuestionnaireOrThrow(Long tenantId, Long questionnaireId) {
        return questionnaireRepository.findByQuestionnaireIdAndTenant_TenantId(questionnaireId, tenantId)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Questionnaire", questionnaireId));
    }

    /**
     * Returns a version of the questionnaire that is safe to mutate: the
     * current one if no responses reference it yet, otherwise a freshly
     * cloned next version (with the current one retired to INACTIVE).
     */
    private AmlQuestionnaire prepareTargetVersion(Tenant tenant, AmlQuestionnaire current) {
        if (!questionnaireResponseRepository.existsByQuestionnaire_QuestionnaireId(current.getQuestionnaireId())) {
            return current;
        }
        return createNextVersion(tenant, current);
    }

    private AmlQuestionnaire createNextVersion(Tenant tenant, AmlQuestionnaire current) {
        AmlQuestionnaire nextVersion = AmlQuestionnaire.builder()
                .tenant(tenant)
                .questionnaireCode(current.getQuestionnaireCode())
                .name(current.getName())
                .description(current.getDescription())
                .version(current.getVersion() + 1)
                .status(QuestionnaireStatus.ACTIVE)
                .effectiveFrom(current.getEffectiveFrom())
                .effectiveTo(current.getEffectiveTo())
                .previousVersion(current)
                .build();
        nextVersion = questionnaireRepository.save(nextVersion);

        List<AmlTenantQuestionnaire> existingMappings = tenantQuestionnaireRepository
                .findByQuestionnaire_QuestionnaireIdOrderByDisplayOrderAsc(current.getQuestionnaireId());
        for (AmlTenantQuestionnaire mapping : existingMappings) {
            AmlTenantQuestionnaire cloned = AmlTenantQuestionnaire.builder()
                    .tenant(mapping.getTenant())
                    .questionnaire(nextVersion)
                    .question(mapping.getQuestion())
                    .mandatory(mapping.isMandatory())
                    .displayOrder(mapping.getDisplayOrder())
                    .conditionalRule(mapping.getConditionalRule())
                    .build();
            tenantQuestionnaireRepository.save(cloned);
        }

        current.setStatus(QuestionnaireStatus.INACTIVE);
        questionnaireRepository.save(current);

        return nextVersion;
    }

    private AmlTenantQuestionnaire attachQuestion(Tenant tenant, AmlQuestionnaire questionnaire,
                                                   AddQuestionRequest request) {
        AmlQuestion question = resolveOrCreateQuestion(tenant, request);

        if (question.getTenant() != null && !question.getTenant().getTenantId().equals(tenant.getTenantId())) {
            throw new IllegalArgumentException("Question " + question.getQuestionCode()
                    + " is tenant-specific to another tenant and cannot be used here");
        }
        if (tenantQuestionnaireRepository.findByQuestionnaire_QuestionnaireIdAndQuestion_QuestionId(
                questionnaire.getQuestionnaireId(), question.getQuestionId()).isPresent()) {
            throw DuplicateResourceException.forField(
                    "QuestionnaireQuestion", "questionCode", question.getQuestionCode());
        }

        AmlTenantQuestionnaire mapping = AmlTenantQuestionnaire.builder()
                .tenant(tenant)
                .questionnaire(questionnaire)
                .question(question)
                .mandatory(Boolean.TRUE.equals(request.getMandatory()))
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .conditionalRule(serializeConditionalRule(request.getConditionalRule()))
                .build();

        return tenantQuestionnaireRepository.save(mapping);
    }

    private AmlQuestion resolveOrCreateQuestion(Tenant tenant, AddQuestionRequest request) {
        if (request.getExistingQuestionCode() != null && !request.getExistingQuestionCode().isBlank()) {
            return questionRepository.findByTenantIsNullAndQuestionCode(request.getExistingQuestionCode())
                    .or(() -> questionRepository.findByTenant_TenantIdAndQuestionCode(
                            tenant.getTenantId(), request.getExistingQuestionCode()))
                    .orElseThrow(() -> ResourceNotFoundException.forEntity(
                            "Question", request.getExistingQuestionCode()));
        }

        if (isBlank(request.getQuestionCode()) || isBlank(request.getQuestionText())
                || request.getQuestionType() == null) {
            throw new IllegalArgumentException(
                    "questionCode, questionText and questionType are required when existingQuestionCode is not set");
        }
        if (questionRepository.findByTenantIsNullAndQuestionCode(request.getQuestionCode()).isPresent()
                || questionRepository.findByTenant_TenantIdAndQuestionCode(
                        tenant.getTenantId(), request.getQuestionCode()).isPresent()) {
            throw DuplicateResourceException.forField("Question", "questionCode", request.getQuestionCode());
        }

        AmlQuestion question = AmlQuestion.builder()
                .tenant(tenant)
                .questionCode(request.getQuestionCode())
                .questionText(request.getQuestionText())
                .questionType(request.getQuestionType())
                .build();
        question = questionRepository.save(question);

        if (request.getOptions() != null) {
            for (QuestionOptionDto optionDto : request.getOptions()) {
                AmlQuestionOption option = AmlQuestionOption.builder()
                        .question(question)
                        .optionCode(optionDto.getOptionCode())
                        .optionLabel(optionDto.getOptionLabel())
                        .displayOrder(optionDto.getDisplayOrder())
                        .build();
                questionOptionRepository.save(option);
            }
        }

        return question;
    }

    private AmlQuestionnaire reload(Long questionnaireId) {
        return questionnaireRepository.findById(questionnaireId)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Questionnaire", questionnaireId));
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String serializeConditionalRule(ConditionalRuleDto rule) {
        if (rule == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(rule);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Invalid conditional rule", ex);
        }
    }

    private ConditionalRuleDto deserializeConditionalRule(String json) {
        if (json == null) {
            return null;
        }
        try {
            return objectMapper.readValue(json, ConditionalRuleDto.class);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Corrupt conditional rule data", ex);
        }
    }

    private QuestionnaireDto toDto(AmlQuestionnaire questionnaire) {
        List<AmlTenantQuestionnaire> mappings = tenantQuestionnaireRepository
                .findByQuestionnaire_QuestionnaireIdOrderByDisplayOrderAsc(questionnaire.getQuestionnaireId());

        List<QuestionnaireQuestionDto> questionDtos = mappings.stream()
                .map(this::toQuestionnaireQuestionDto)
                .toList();

        return QuestionnaireDto.builder()
                .questionnaireId(questionnaire.getQuestionnaireId())
                .tenantId(questionnaire.getTenant().getTenantId())
                .questionnaireCode(questionnaire.getQuestionnaireCode())
                .name(questionnaire.getName())
                .description(questionnaire.getDescription())
                .version(questionnaire.getVersion())
                .status(questionnaire.getStatus())
                .effectiveFrom(questionnaire.getEffectiveFrom())
                .effectiveTo(questionnaire.getEffectiveTo())
                .previousVersionId(questionnaire.getPreviousVersion() != null
                        ? questionnaire.getPreviousVersion().getQuestionnaireId() : null)
                .questions(questionDtos)
                .build();
    }

    private QuestionnaireQuestionDto toQuestionnaireQuestionDto(AmlTenantQuestionnaire mapping) {
        return QuestionnaireQuestionDto.builder()
                .tenantQuestionnaireId(mapping.getTenantQuestionnaireId())
                .question(toQuestionDto(mapping.getQuestion()))
                .mandatory(mapping.isMandatory())
                .displayOrder(mapping.getDisplayOrder())
                .conditionalRule(deserializeConditionalRule(mapping.getConditionalRule()))
                .build();
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
                .active(question.isActive())
                .options(options)
                .build();
    }
}
