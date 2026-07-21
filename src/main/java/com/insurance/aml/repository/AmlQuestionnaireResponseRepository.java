package com.insurance.aml.repository;

import com.insurance.aml.entity.AmlQuestionnaireResponse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AmlQuestionnaireResponseRepository extends JpaRepository<AmlQuestionnaireResponse, Long> {

    boolean existsByQuestionnaireTenant_QuestionnaireTenantId(Long questionnaireTenantId);

    List<AmlQuestionnaireResponse> findByTenant_TenantIdAndCustomer_CustomerId(Long tenantId, Long customerId);

    Optional<AmlQuestionnaireResponse> findTopByTenant_TenantIdAndCustomer_CustomerIdOrderByCreatedAtDesc(
            Long tenantId, Long customerId);

    Optional<AmlQuestionnaireResponse> findTopByCustomer_CustomerIdOrderByCreatedAtDesc(Long customerId);
}
