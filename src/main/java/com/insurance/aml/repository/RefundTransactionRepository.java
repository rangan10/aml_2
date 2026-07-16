package com.insurance.aml.repository;

import com.insurance.aml.entity.RefundTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RefundTransactionRepository extends JpaRepository<RefundTransaction, Long> {

    List<RefundTransaction> findByPolicy_PolicyId(Long policyId);
}
