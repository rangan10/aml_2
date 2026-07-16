package com.insurance.aml.repository;

import com.insurance.aml.entity.FreeLookCancellation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface FreeLookCancellationRepository extends JpaRepository<FreeLookCancellation, Long> {

    @Query("""
            SELECT f FROM FreeLookCancellation f
            WHERE f.customer.customerId = :customerId
            AND f.cancellationDate BETWEEN :from AND :to
            """)
    List<FreeLookCancellation> findByCustomerAndDateBetween(@Param("customerId") Long customerId,
                                                             @Param("from") LocalDate from,
                                                             @Param("to") LocalDate to);
}
