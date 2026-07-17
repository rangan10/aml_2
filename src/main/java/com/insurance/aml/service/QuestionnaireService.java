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
import com.insurance.aml.entity.AmlQuestionnaireTenant;
import com.insurance.aml.entity.AmlTenantQuestionnaire;
import com.insurance.aml.enums.QuestionnaireStatus;
import com.insurance.aml.entity.Tenant;
import com.insurance.aml.exception.DuplicateResourceException;
import com.insurance.aml.exception.ResourceNotFoundException;
import com.insurance.aml.repository.AmlQuestionOptionRepository;
import com.insurance.aml.repository.AmlQuestionRepository;
import com.insurance.aml.repository.AmlQuestionnaireRepository;
import com.insurance.aml.repository.AmlQuestionnaireResponseRepository;
import com.insurance.aml.repository.AmlQuestionnaireTenantRepository;
import com.insurance.aml.repository.AmlTenantQuestionnaireRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Manages shared questionnaire templates: creation, tenant adoption,
 * attaching/removing/reconfiguring questions, and the version-history
 * mechanics. A questionnaire is a tenant-agnostic definition (see
 * {@link AmlQuestionnaire}); {@link AmlQuestionnaireTenant} rows track which
 * tenants have adopted it, and {@link AmlTenantQuestionnaire} rows carry each
 * tenant's own configuration for its questions. A structural change to an
 * existing questionnaire mutates it in place until customer responses have
 * been recorded against it; from that point on, structural changes create a
 * new version, and every tenant currently assigned to the old version moves
 * to the new one together, so historical responses keep referencing the
 * exact version they were answered against.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class QuestionnaireService {

    private final AmlQuestionnaireRepository questionnaireRepository;
    private final AmlQuestionRepository questionRepository;
    private final AmlQuestionOptionRepository questionOptionRepository;
    private final AmlTenantQuestionnaireRepository tenantQuestionnaireRepository;
    private final AmlQuestionnaireTenantRepository questionnaireTenantRepository;
    private final AmlQuestionnaireResponseRepository questionnaireResponseRepository;
    private final TenantService tenantService;
    private final ObjectMapper objectMapper;

    public QuestionnaireDto createQuestionnaire(Long tenantId, CreateQuestionnaireRequest request) {
        Tenant tenant = tenantService.findTenantOrThrow(tenantId);

        if (questionnaireRepository.findTopByQuestionnaireCodeOrderByVersionDesc(
                request.getQuestionnaireCode()).isPresent()) {
            throw DuplicateResourceException.forField(
                    "Questionnaire", "questionnaireCode", request.getQuestionnaireCode());
        }
        if (request.getEffectiveTo() != null && request.getEffectiveTo().isBefore(request.getEffectiveFrom())) {
            throw new IllegalArgumentException("effectiveTo must not be before effectiveFrom");
        }

        AmlQuestionnaire questionnaire = AmlQuestionnaire.builder()
                .questionnaireCode(request.getQuestionnaireCode())
                .name(request.getName())
                .description(request.getDescription())
                .version(1)
                .status(QuestionnaireStatus.ACTIVE)
                .effectiveFrom(request.getEffectiveFrom())
                .effectiveTo(request.getEffectiveTo())
                .build();
        questionnaire = questionnaireRepository.save(questionnaire);

        questionnaireTenantRepository.save(AmlQuestionnaireTenant.builder()
                .questionnaire(questionnaire)
                .tenant(tenant)
                .build());

        if (request.getQuestions() != null) {
            for (AddQuestionRequest questionRequest : request.getQuestions()) {
                attachQuestion(tenant, questionnaire, questionRequest);
            }
        }

        return toDto(questionnaire, tenantId);
    }

    /**
     * Adopts an existing shared questionnaire (created by any tenant) for
     * {@code tenantId}, so it shows up in that tenant's questionnaire list
     * and can be configured with its own question set.
     */
    public QuestionnaireDto assignQuestionnaire(Long tenantId, Long questionnaireId) {
        Tenant tenant = tenantService.findTenantOrThrow(tenantId);
        AmlQuestionnaire questionnaire = questionnaireRepository.findById(questionnaireId)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Questionnaire", questionnaireId));

        if (questionnaireTenantRepository.existsByQuestionnaire_QuestionnaireIdAndTenant_TenantId(
                questionnaireId, tenantId)) {
            throw DuplicateResourceException.forField(
                    "QuestionnaireTenant", "questionnaireId", String.valueOf(questionnaireId));
        }

        questionnaireTenantRepository.save(AmlQuestionnaireTenant.builder()
                .questionnaire(questionnaire)
                .tenant(tenant)
                .build());

        return toDto(questionnaire, tenantId);
    }

    public QuestionnaireDto addQuestion(Long tenantId, Long questionnaireId, AddQuestionRequest request) {
        Tenant tenant = tenantService.findTenantOrThrow(tenantId);
        AmlQuestionnaire current = findQuestionnaireOrThrow(tenantId, questionnaireId);

        AmlQuestionnaire target = prepareTargetVersion(current);
        attachQuestion(tenant, target, request);

        return toDto(reload(target.getQuestionnaireId()), tenantId);
    }

    public QuestionnaireDto removeQuestion(Long tenantId, Long questionnaireId, Long questionId) {
        AmlQuestionnaire current = findQuestionnaireOrThrow(tenantId, questionnaireId);
        AmlQuestionnaire target = prepareTargetVersion(current);

        AmlTenantQuestionnaire mapping = tenantQuestionnaireRepository
                .findByTenant_TenantIdAndQuestionnaire_QuestionnaireIdAndQuestion_QuestionId(
                        tenantId, target.getQuestionnaireId(), questionId)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("QuestionnaireQuestion", questionId));
        tenantQuestionnaireRepository.delete(mapping);

        return toDto(reload(target.getQuestionnaireId()), tenantId);
    }

    public QuestionnaireDto modifyQuestionConfig(Long tenantId, Long questionnaireId, Long questionId,
                                                  ModifyQuestionConfigRequest request) {
        AmlQuestionnaire current = findQuestionnaireOrThrow(tenantId, questionnaireId);
        AmlQuestionnaire target = prepareTargetVersion(current);

        AmlTenantQuestionnaire mapping = tenantQuestionnaireRepository
                .findByTenant_TenantIdAndQuestionnaire_QuestionnaireIdAndQuestion_QuestionId(
                        tenantId, target.getQuestionnaireId(), questionId)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("QuestionnaireQuestion", questionId));

        mapping.setMandatory(Boolean.TRUE.equals(request.getMandatory()));
        mapping.setDisplayOrder(request.getDisplayOrder());
        mapping.setConditionalRule(serializeConditionalRule(request.getConditionalRule()));
        tenantQuestionnaireRepository.save(mapping);

        return toDto(reload(target.getQuestionnaireId()), tenantId);
    }

    @Transactional(readOnly = true)
    public List<QuestionnaireDto> getQuestionnaires(Long tenantId) {
        tenantService.findTenantOrThrow(tenantId);
        return questionnaireTenantRepository.findByTenant_TenantId(tenantId).stream()
                .map(AmlQuestionnaireTenant::getQuestionnaire)
                .filter(q -> q.getStatus() != QuestionnaireStatus.INACTIVE)
                .map(q -> toDto(q, tenantId))
                .toList();
    }

    @Transactional(readOnly = true)
    public QuestionnaireDto getQuestionnaire(Long tenantId, Long questionnaireId) {
        return toDto(findQuestionnaireOrThrow(tenantId, questionnaireId), tenantId);
    }

    AmlQuestionnaire findQuestionnaireOrThrow(Long tenantId, Long questionnaireId) {
        return questionnaireTenantRepository.findByQuestionnaire_QuestionnaireIdAndTenant_TenantId(
                        questionnaireId, tenantId)
                .map(AmlQuestionnaireTenant::getQuestionnaire)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Questionnaire", questionnaireId));
    }

    /**
     * Returns a version of the questionnaire that is safe to mutate: the
     * current one if no responses reference it yet, otherwise a freshly
     * cloned next version (with the current one retired to INACTIVE). Since
     * a questionnaire is shared, every tenant assigned to the current
     * version moves to the next version together.
     */
    private AmlQuestionnaire prepareTargetVersion(AmlQuestionnaire current) {
        if (!questionnaireResponseRepository.existsByQuestionnaire_QuestionnaireId(current.getQuestionnaireId())) {
            return current;
        }
        return createNextVersion(current);
    }

    private AmlQuestionnaire createNextVersion(AmlQuestionnaire current) {
        AmlQuestionnaire nextVersion = AmlQuestionnaire.builder()
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

        List<AmlQuestionnaireTenant> assignments = questionnaireTenantRepository
                .findByQuestionnaire_QuestionnaireId(current.getQuestionnaireId());
        for (AmlQuestionnaireTenant assignment : assignments) {
            assignment.setQuestionnaire(nextVersion);
            questionnaireTenantRepository.save(assignment);
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
        if (tenantQuestionnaireRepository.findByTenant_TenantIdAndQuestionnaire_QuestionnaireIdAndQuestion_QuestionId(
                tenant.getTenantId(), questionnaire.getQuestionnaireId(), question.getQuestionId()).isPresent()) {
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
                || request.getQuestionType() == null || request.getCategory() == null) {
            throw new IllegalArgumentException(
                    "questionCode, questionText, questionType and category are required when "
                            + "existingQuestionCode is not set");
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
                .category(request.getCategory())
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

    private QuestionnaireDto toDto(AmlQuestionnaire questionnaire, Long tenantId) {
        List<AmlTenantQuestionnaire> mappings = tenantQuestionnaireRepository
                .findByTenant_TenantIdAndQuestionnaire_QuestionnaireIdOrderByDisplayOrderAsc(
                        tenantId, questionnaire.getQuestionnaireId());

        List<QuestionnaireQuestionDto> questionDtos = mappings.stream()
                .map(this::toQuestionnaireQuestionDto)
                .toList();

        return QuestionnaireDto.builder()
                .questionnaireId(questionnaire.getQuestionnaireId())
                .tenantId(tenantId)
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
                .category(question.getCategory())
                .active(question.isActive())
                .options(options)
                .build();
    }
}
