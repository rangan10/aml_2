package com.insurance.aml.repository;

import com.insurance.aml.entity.AmlTenantQuestionnaire;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AmlTenantQuestionnaireRepository extends JpaRepository<AmlTenantQuestionnaire, Long> {

    List<AmlTenantQuestionnaire> findByQuestionnaire_QuestionnaireIdOrderByDisplayOrderAsc(Long questionnaireId);

    Optional<AmlTenantQuestionnaire> findByQuestionnaire_QuestionnaireIdAndQuestion_QuestionId(
            Long questionnaireId, Long questionId);
}
