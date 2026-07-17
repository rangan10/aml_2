package com.insurance.aml.repository;

import com.insurance.aml.entity.AmlQuestionnaire;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AmlQuestionnaireRepository extends JpaRepository<AmlQuestionnaire, Long> {

    Optional<AmlQuestionnaire> findTopByQuestionnaireCodeOrderByVersionDesc(String questionnaireCode);
}
