package com.insurance.aml.repository;

import com.insurance.aml.entity.MCAScreeningResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MCAScreeningResultRepository extends JpaRepository<MCAScreeningResult, Long> {

    List<MCAScreeningResult> findByCustomer_CustomerIdAndMatchFoundTrue(Long customerId);
}
