package com.insurance.aml.repository;

import com.insurance.aml.entity.AmlQuestionResponse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AmlQuestionResponseRepository extends JpaRepository<AmlQuestionResponse, Long> {

    List<AmlQuestionResponse> findByResponse_ResponseId(Long responseId);
}
