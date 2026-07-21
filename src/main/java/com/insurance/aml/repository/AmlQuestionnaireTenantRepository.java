package com.insurance.aml.repository;

import com.insurance.aml.entity.AmlQuestionnaireTenant;
import com.insurance.aml.enums.QuestionnaireStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AmlQuestionnaireTenantRepository extends JpaRepository<AmlQuestionnaireTenant, Long> {

    List<AmlQuestionnaireTenant> findByTenant_TenantIdAndStatus(Long tenantId, QuestionnaireStatus status);

    Optional<AmlQuestionnaireTenant> findByQuestionnaire_QuestionnaireIdAndTenant_TenantIdAndStatus(
            Long questionnaireId, Long tenantId, QuestionnaireStatus status);

    Optional<AmlQuestionnaireTenant> findTopByQuestionnaire_QuestionnaireIdAndTenant_TenantIdOrderByVersionDesc(
            Long questionnaireId, Long tenantId);

    boolean existsByQuestionnaire_QuestionnaireIdAndTenant_TenantId(Long questionnaireId, Long tenantId);
}
