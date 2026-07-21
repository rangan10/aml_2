package com.insurance.aml.repository;

import com.insurance.aml.entity.AmlTenantQuestionnaire;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AmlTenantQuestionnaireRepository extends JpaRepository<AmlTenantQuestionnaire, Long> {

    List<AmlTenantQuestionnaire> findByQuestionnaireTenant_QuestionnaireTenantIdOrderByDisplayOrderAsc(
            Long questionnaireTenantId);

    Optional<AmlTenantQuestionnaire> findByQuestionnaireTenant_QuestionnaireTenantIdAndQuestion_QuestionId(
            Long questionnaireTenantId, Long questionId);
}
