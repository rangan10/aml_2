package com.insurance.aml.repository;

import com.insurance.aml.entity.AmlQuestion;
import com.insurance.aml.enums.QuestionCategory;
import com.insurance.aml.enums.QuestionScope;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AmlQuestionRepository extends JpaRepository<AmlQuestion, Long> {

    Optional<AmlQuestion> findByQuestionScopeAndQuestionCode(
            QuestionScope questionScope,
            String questionCode);

    List<AmlQuestion> findByQuestionScope(
            QuestionScope questionScope);

    List<AmlQuestion> findByQuestionScopeAndCategory(
            QuestionScope questionScope,
            QuestionCategory category);

    // Tenant Specific Questions

    @Query("""
        SELECT aqt.question
        FROM AmlQuestionTenant aqt
        WHERE aqt.tenantId = :tenantId
          AND aqt.question.questionCode = :questionCode
          AND aqt.active = true
        ORDER BY aqt.versionNo DESC
    """)
    List<AmlQuestion> findQuestionsByTenantAndQuestionCode(
            @Param("tenantId") Long tenantId,
            @Param("questionCode") String questionCode);

    @Query("""
        SELECT aqt.question
        FROM AmlQuestionTenant aqt
        WHERE aqt.tenantId = :tenantId
          AND aqt.active = true
    """)
    List<AmlQuestion> findByTenantId(
            @Param("tenantId") Long tenantId);

    @Query("""
        SELECT aqt.question
        FROM AmlQuestionTenant aqt
        WHERE aqt.tenantId = :tenantId
          AND aqt.question.category = :category
          AND aqt.active = true
    """)
    List<AmlQuestion> findQuestionsByTenantAndCategory(
            @Param("tenantId") Long tenantId,
            @Param("category") QuestionCategory category);

    @Query("""
        SELECT aqt.question
        FROM AmlQuestionTenant aqt
        WHERE aqt.tenantId = :tenantId
          AND aqt.question.questionCode = :questionCode
          AND aqt.active = true
    """)
    Optional<AmlQuestion> findByTenantIdAndQuestionCode(
            @Param("tenantId") Long tenantId,
            @Param("questionCode") String questionCode);

}
