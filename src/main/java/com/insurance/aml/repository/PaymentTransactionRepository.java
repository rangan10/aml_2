package com.insurance.aml.repository;

import com.insurance.aml.entity.PaymentMode;
import com.insurance.aml.entity.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {

    List<PaymentTransaction> findByPolicy_PolicyId(Long policyId);

    @Query("""
            SELECT p FROM PaymentTransaction p
            WHERE p.policy.customer.customerId = :customerId
            AND p.paymentMode IN :modes
            AND p.paymentDate BETWEEN :from AND :to
            """)
    List<PaymentTransaction> findByCustomerAndModesBetween(@Param("customerId") Long customerId,
                                                            @Param("modes") List<PaymentMode> modes,
                                                            @Param("from") LocalDateTime from,
                                                            @Param("to") LocalDateTime to);
}
