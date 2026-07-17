package com.insurance.aml.repository;

import com.insurance.aml.entity.AmlQuestion;
import com.insurance.aml.enums.QuestionCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AmlQuestionRepository extends JpaRepository<AmlQuestion, Long> {

    Optional<AmlQuestion> findByTenantIsNullAndQuestionCode(String questionCode);

    Optional<AmlQuestion> findByTenant_TenantIdAndQuestionCode(Long tenantId, String questionCode);

    List<AmlQuestion> findByTenantIsNull();

    List<AmlQuestion> findByTenant_TenantId(Long tenantId);

    List<AmlQuestion> findByTenantIsNullAndCategory(QuestionCategory category);

    List<AmlQuestion> findByTenant_TenantIdAndCategory(Long tenantId, QuestionCategory category);
}
