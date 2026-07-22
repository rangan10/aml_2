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
import com.insurance.aml.enums.QuestionScope;
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

import java.time.LocalDate;
import java.util.List;

/**
 * Manages questionnaires. A questionnaire is a shared, tenant-agnostic catalog
 * entry ({@link AmlQuestionnaire}) identified by its code; each tenant that
 * adopts it owns its own versioned instance ({@link AmlQuestionnaireTenant})
 * carrying that tenant's version lineage, lifecycle and question configuration
 * ({@link AmlTenantQuestionnaire}). Versioning is therefore per tenant: a
 * structural change (add/remove/reconfigure a question) mutates the tenant's
 * current instance in place until customer responses have been recorded
 * against it; from that point on, a structural change creates a new version of
 * that tenant's instance only, so historical responses keep referencing the
 * exact tenant-version they were answered against and no other tenant is
 * affected.
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

        if (request.getEffectiveTo() != null && request.getEffectiveTo().isBefore(request.getEffectiveFrom())) {
            throw new IllegalArgumentException("effectiveTo must not be before effectiveFrom");
        }

        AmlQuestionnaire questionnaire = questionnaireRepository
                .findByQuestionnaireCode(request.getQuestionnaireCode())
                .orElse(null);
        if (questionnaire == null) {
            questionnaire = questionnaireRepository.save(AmlQuestionnaire.builder()
                    .questionnaireCode(request.getQuestionnaireCode())
                    .name(request.getName())
                    .description(request.getDescription())
                    .build());
        } else if (questionnaireTenantRepository.existsByQuestionnaire_QuestionnaireIdAndTenant_TenantId(
                questionnaire.getQuestionnaireId(), tenantId)) {
            throw DuplicateResourceException.forField(
                    "Questionnaire", "questionnaireCode", request.getQuestionnaireCode());
        }

        AmlQuestionnaireTenant instance = questionnaireTenantRepository.save(AmlQuestionnaireTenant.builder()
                .questionnaire(questionnaire)
                .tenant(tenant)
                .version(1)
                .status(QuestionnaireStatus.ACTIVE)
                .effectiveFrom(request.getEffectiveFrom())
                .effectiveTo(request.getEffectiveTo())
                .build());

        if (request.getQuestions() != null) {
            for (AddQuestionRequest questionRequest : request.getQuestions()) {
                attachQuestion(tenant, instance, questionRequest);
            }
        }

        return toDto(reload(instance.getQuestionnaireTenantId()));
    }

    /**
     * Adopts an existing shared questionnaire (its catalog entry) for
     * {@code tenantId}, creating that tenant's own version-1 instance (with no
     * questions yet) so it shows up in the tenant's questionnaire list and can
     * be configured independently of every other tenant.
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

        AmlQuestionnaireTenant instance = questionnaireTenantRepository.save(AmlQuestionnaireTenant.builder()
                .questionnaire(questionnaire)
                .tenant(tenant)
                .version(1)
                .status(QuestionnaireStatus.ACTIVE)
                .effectiveFrom(LocalDate.now())
                .build());

        return toDto(reload(instance.getQuestionnaireTenantId()));
    }

    public QuestionnaireDto addQuestion(Long tenantId, Long questionnaireId, AddQuestionRequest request) {
        Tenant tenant = tenantService.findTenantOrThrow(tenantId);
        AmlQuestionnaireTenant current = findQuestionnaireOrThrow(tenantId, questionnaireId);

        AmlQuestionnaireTenant target = prepareTargetVersion(current);
        attachQuestion(tenant, target, request);

        return toDto(reload(target.getQuestionnaireTenantId()));
    }

    public QuestionnaireDto removeQuestion(Long tenantId, Long questionnaireId, Long questionId) {
        AmlQuestionnaireTenant current = findQuestionnaireOrThrow(tenantId, questionnaireId);
        AmlQuestionnaireTenant target = prepareTargetVersion(current);

        AmlTenantQuestionnaire mapping = tenantQuestionnaireRepository
                .findByQuestionnaireTenant_QuestionnaireTenantIdAndQuestion_QuestionId(
                        target.getQuestionnaireTenantId(), questionId)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("QuestionnaireQuestion", questionId));
        tenantQuestionnaireRepository.delete(mapping);

        return toDto(reload(target.getQuestionnaireTenantId()));
    }

    public QuestionnaireDto modifyQuestionConfig(Long tenantId, Long questionnaireId, Long questionId,
                                                  ModifyQuestionConfigRequest request) {
        AmlQuestionnaireTenant current = findQuestionnaireOrThrow(tenantId, questionnaireId);
        AmlQuestionnaireTenant target = prepareTargetVersion(current);

        AmlTenantQuestionnaire mapping = tenantQuestionnaireRepository
                .findByQuestionnaireTenant_QuestionnaireTenantIdAndQuestion_QuestionId(
                        target.getQuestionnaireTenantId(), questionId)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("QuestionnaireQuestion", questionId));

        mapping.setMandatory(Boolean.TRUE.equals(request.getMandatory()));
        mapping.setDisplayOrder(request.getDisplayOrder());
        mapping.setConditionalRule(serializeConditionalRule(request.getConditionalRule()));
        tenantQuestionnaireRepository.save(mapping);

        return toDto(reload(target.getQuestionnaireTenantId()));
    }

    @Transactional(readOnly = true)
    public List<QuestionnaireDto> getQuestionnaires(Long tenantId) {
        tenantService.findTenantOrThrow(tenantId);
        return questionnaireTenantRepository.findByTenant_TenantIdAndStatus(tenantId, QuestionnaireStatus.ACTIVE)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public QuestionnaireDto getQuestionnaire(Long tenantId, Long questionnaireId) {
        return toDto(findQuestionnaireOrThrow(tenantId, questionnaireId));
    }

    /**
     * Resolves the tenant's current (ACTIVE) instance of the shared
     * questionnaire identified by {@code questionnaireId} (its catalog id).
     */
    AmlQuestionnaireTenant findQuestionnaireOrThrow(Long tenantId, Long questionnaireId) {
        return questionnaireTenantRepository.findByQuestionnaire_QuestionnaireIdAndTenant_TenantIdAndStatus(
                        questionnaireId, tenantId, QuestionnaireStatus.ACTIVE)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Questionnaire", questionnaireId));
    }

    /**
     * Returns an instance that is safe to mutate: the tenant's current one if
     * no responses reference it yet, otherwise a freshly cloned next version
     * (with the current one retired to INACTIVE). Only this tenant's instance
     * is versioned; other tenants sharing the questionnaire are untouched.
     */
    private AmlQuestionnaireTenant prepareTargetVersion(AmlQuestionnaireTenant current) {
        if (!questionnaireResponseRepository.existsByQuestionnaireTenant_QuestionnaireTenantId(
                current.getQuestionnaireTenantId())) {
            return current;
        }
        return createNextVersion(current);
    }

    private AmlQuestionnaireTenant createNextVersion(AmlQuestionnaireTenant current) {
        AmlQuestionnaireTenant nextVersion = questionnaireTenantRepository.save(AmlQuestionnaireTenant.builder()
                .questionnaire(current.getQuestionnaire())
                .tenant(current.getTenant())
                .version(current.getVersion() + 1)
                .status(QuestionnaireStatus.ACTIVE)
                .effectiveFrom(current.getEffectiveFrom())
                .effectiveTo(current.getEffectiveTo())
                .previousVersion(current)
                .build());

        List<AmlTenantQuestionnaire> existingMappings = tenantQuestionnaireRepository
                .findByQuestionnaireTenant_QuestionnaireTenantIdOrderByDisplayOrderAsc(
                        current.getQuestionnaireTenantId());
        for (AmlTenantQuestionnaire mapping : existingMappings) {
            tenantQuestionnaireRepository.save(AmlTenantQuestionnaire.builder()
                    .questionnaireTenant(nextVersion)
                    .question(mapping.getQuestion())
                    .mandatory(mapping.isMandatory())
                    .displayOrder(mapping.getDisplayOrder())
                    .conditionalRule(mapping.getConditionalRule())
                    .build());
        }

        current.setStatus(QuestionnaireStatus.INACTIVE);
        current.setActive(false);
        questionnaireTenantRepository.save(current);

        return nextVersion;
    }

    private AmlTenantQuestionnaire attachQuestion(Tenant tenant, AmlQuestionnaireTenant instance,
                                                   AddQuestionRequest request) {
        AmlQuestion question = resolveOrCreateQuestion(tenant, request);

//        if (question.getTenant() != null && !question.getTenant().getTenantId().equals(tenant.getTenantId())) {
//            throw new IllegalArgumentException("Question " + question.getQuestionCode()
//                    + " is tenant-specific to another tenant and cannot be used here");
//        }
        if (tenantQuestionnaireRepository.findByQuestionnaireTenant_QuestionnaireTenantIdAndQuestion_QuestionId(
                instance.getQuestionnaireTenantId(), question.getQuestionId()).isPresent()) {
            throw DuplicateResourceException.forField(
                    "QuestionnaireQuestion", "questionCode", question.getQuestionCode());
        }

        AmlTenantQuestionnaire mapping = AmlTenantQuestionnaire.builder()
                .questionnaireTenant(instance)
                .question(question)
                .mandatory(Boolean.TRUE.equals(request.getMandatory()))
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .conditionalRule(serializeConditionalRule(request.getConditionalRule()))
                .build();

        return tenantQuestionnaireRepository.save(mapping);
    }

    private AmlQuestion resolveOrCreateQuestion(Tenant tenant, AddQuestionRequest request) {
        if (request.getExistingQuestionCode() != null && !request.getExistingQuestionCode().isBlank()) {
            return questionRepository.findByQuestionScopeAndQuestionCode(QuestionScope.GLOBAL,request.getExistingQuestionCode())
                    .or(() -> questionRepository.findByTenantIdAndQuestionCode(
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
        if (questionRepository.findByQuestionScopeAndQuestionCode(QuestionScope.GLOBAL,request.getQuestionCode()).isPresent()
                || questionRepository.findByTenantIdAndQuestionCode(
                        tenant.getTenantId(), request.getQuestionCode()).isPresent()) {
            throw DuplicateResourceException.forField("Question", "questionCode", request.getQuestionCode());
        }

        AmlQuestion question = AmlQuestion.builder()
//                .tenant(tenant)
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

    private AmlQuestionnaireTenant reload(Long questionnaireTenantId) {
        return questionnaireTenantRepository.findById(questionnaireTenantId)
                .orElseThrow(() -> ResourceNotFoundException.forEntity(
                        "QuestionnaireTenant", questionnaireTenantId));
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

    private QuestionnaireDto toDto(AmlQuestionnaireTenant instance) {
        AmlQuestionnaire questionnaire = instance.getQuestionnaire();

        List<AmlTenantQuestionnaire> mappings = tenantQuestionnaireRepository
                .findByQuestionnaireTenant_QuestionnaireTenantIdOrderByDisplayOrderAsc(
                        instance.getQuestionnaireTenantId());

        List<QuestionnaireQuestionDto> questionDtos = mappings.stream()
                .map(this::toQuestionnaireQuestionDto)
                .toList();

        return QuestionnaireDto.builder()
                .questionnaireId(questionnaire.getQuestionnaireId())
                .tenantId(instance.getTenant().getTenantId())
                .questionnaireCode(questionnaire.getQuestionnaireCode())
                .name(questionnaire.getName())
                .description(questionnaire.getDescription())
                .version(instance.getVersion())
                .status(instance.getStatus())
                .effectiveFrom(instance.getEffectiveFrom())
                .effectiveTo(instance.getEffectiveTo())
                .previousVersionId(instance.getPreviousVersion() != null
                        ? instance.getPreviousVersion().getQuestionnaireTenantId() : null)
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
}
