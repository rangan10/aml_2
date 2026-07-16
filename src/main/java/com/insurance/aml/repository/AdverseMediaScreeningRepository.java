package com.insurance.aml.repository;

import com.insurance.aml.entity.AdverseMediaScreening;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdverseMediaScreeningRepository extends JpaRepository<AdverseMediaScreening, Long> {

    List<AdverseMediaScreening> findByCustomer_CustomerIdAndMatchFoundTrue(Long customerId);
}
