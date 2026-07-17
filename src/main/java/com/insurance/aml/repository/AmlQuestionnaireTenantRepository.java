package com.insurance.aml.repository;

import com.insurance.aml.entity.AmlQuestionnaireTenant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AmlQuestionnaireTenantRepository extends JpaRepository<AmlQuestionnaireTenant, Long> {

    List<AmlQuestionnaireTenant> findByTenant_TenantId(Long tenantId);

    List<AmlQuestionnaireTenant> findByQuestionnaire_QuestionnaireId(Long questionnaireId);

    Optional<AmlQuestionnaireTenant> findByQuestionnaire_QuestionnaireIdAndTenant_TenantId(
            Long questionnaireId, Long tenantId);

    boolean existsByQuestionnaire_QuestionnaireIdAndTenant_TenantId(Long questionnaireId, Long tenantId);
}
