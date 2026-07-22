package com.insurance.aml.repository;

import com.insurance.aml.entity.AmlQuestionResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AmlQuestionResponseRepository extends JpaRepository<AmlQuestionResponse, Long> {

    @Query(value = """
    SELECT *
    FROM aml_question_response r
    WHERE r.question_response_id IN (
        SELECT question_response_id
        FROM (
            SELECT question_response_id,
                   ROW_NUMBER() OVER (
                       PARTITION BY question_id
                       ORDER BY created_at DESC
                   ) rn
            FROM aml_question_response
            WHERE user_id = :userId
              AND tenant_id = :tenantId
        ) t
        WHERE rn = 1
    )
    """, nativeQuery = true)
    List<AmlQuestionResponse> findLatestResponsesByUserAndTenant(
            Long userId,
            Long tenantId);}
