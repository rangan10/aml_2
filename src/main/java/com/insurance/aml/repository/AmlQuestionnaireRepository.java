package com.insurance.aml.repository;

import com.insurance.aml.entity.AmlQuestionnaire;
import com.insurance.aml.entity.QuestionnaireStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AmlQuestionnaireRepository extends JpaRepository<AmlQuestionnaire, Long> {

    List<AmlQuestionnaire> findByTenant_TenantIdAndStatusNot(Long tenantId, QuestionnaireStatus status);

    Optional<AmlQuestionnaire> findByTenant_TenantIdAndQuestionnaireCodeAndStatus(
            Long tenantId, String questionnaireCode, QuestionnaireStatus status);

    Optional<AmlQuestionnaire> findTopByTenant_TenantIdAndQuestionnaireCodeOrderByVersionDesc(
            Long tenantId, String questionnaireCode);

    Optional<AmlQuestionnaire> findByQuestionnaireIdAndTenant_TenantId(Long questionnaireId, Long tenantId);
}
