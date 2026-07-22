package com.insurance.aml.repository;

import com.insurance.aml.entity.AmlQuestionOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AmlQuestionOptionRepository extends JpaRepository<AmlQuestionOption, Long> {

    List<AmlQuestionOption> findByQuestion_QuestionIdOrderByDisplayOrderAsc(Long questionId);

    Optional<AmlQuestionOption> findByQuestion_QuestionIdAndOptionCode(Long questionId, String optionCode);

    Optional<AmlQuestionOption> findByQuestion_QuestionIdAndOptionId(Long questionId, Long optionId);

}
