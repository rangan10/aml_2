package com.insurance.aml.repository;

import com.insurance.aml.entity.AmlQuestion;
import com.insurance.aml.entity.AmlQuestionTenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AmlQuestionTenantRepo extends JpaRepository<AmlQuestionTenant,Long> {

    @Query("""
    SELECT aqt.question
    FROM AmlQuestionTenant aqt
    WHERE aqt.tenant.tenantId = :tenantId
      AND aqt.question.questionCode = :questionCode
      AND aqt.active = true
      AND aqt.versionNo = (
          SELECT MAX(aqt2.versionNo)
          FROM AmlQuestionTenant aqt2
          WHERE aqt2.tenant.tenantId = :tenantId
            AND aqt2.question.questionCode = :questionCode
      )
""")
    Optional<AmlQuestion> findByTenantIdAndQuestionCode(
            @Param("tenantId") Long tenantId,
            @Param("questionCode") String questionCode);

    @Query("""
    SELECT q
    FROM AmlQuestion q
    WHERE q.questionId = :questionId
      AND q.active = true
      AND (
          q.questionScope = 'GLOBAL'
          OR EXISTS (
              SELECT 1
              FROM AmlQuestionTenant aqt
              WHERE aqt.question = q
                AND aqt.tenant.tenantId = :tenantId
                AND aqt.active = true
                AND aqt.versionNo = (
                    SELECT MAX(aqt2.versionNo)
                    FROM AmlQuestionTenant aqt2
                    WHERE aqt2.question = q
                      AND aqt2.tenant.tenantId = :tenantId
                )
          )
      )
""")
    Optional<AmlQuestion> findByTenantIdAndQuestionId(
            @Param("tenantId") Long tenantId,
            @Param("questionId") Long questionId
    );


}
