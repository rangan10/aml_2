package com.insurance.aml.repository;

import com.insurance.aml.entity.AddressChangeLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AddressChangeLogRepository extends JpaRepository<AddressChangeLog, Long> {

    List<AddressChangeLog> findByCustomer_CustomerIdOrderByChangeDateAsc(Long customerId);

    long countByCustomer_CustomerId(Long customerId);
}
