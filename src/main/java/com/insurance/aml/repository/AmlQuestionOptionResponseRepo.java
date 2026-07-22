package com.insurance.aml.repository;

import com.insurance.aml.entity.AmlQuestionOptionResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AmlQuestionOptionResponseRepo extends JpaRepository<AmlQuestionOptionResponse,Long> {
}
