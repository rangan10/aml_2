package com.insurance.aml.repository;

import com.insurance.aml.entity.ClaimEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClaimEventRepository extends JpaRepository<ClaimEvent, Long> {

    List<ClaimEvent> findByPolicy_PolicyId(Long policyId);
}
