package com.insurance.aml.repository;

import com.insurance.aml.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByCustomerCode(String customerCode);

    List<Customer> findByPanNumber(String panNumber);

    List<Customer> findByMobileNumber(String mobileNumber);

    List<Customer> findByEmailIgnoreCase(String email);

    @Query("SELECT c FROM Customer c WHERE c.addressCurrent = :address")
    List<Customer> findByAddressCurrent(@Param("address") String address);

    @Query("""
            SELECT c FROM Customer c
            WHERE c.customerId <> :customerId
            AND (c.panNumber = :pan OR c.mobileNumber = :mobile
                 OR LOWER(c.email) = LOWER(:email) OR c.addressCurrent = :address)
            """)
    List<Customer> findPotentialDuplicates(@Param("customerId") Long customerId,
                                            @Param("pan") String pan,
                                            @Param("mobile") String mobile,
                                            @Param("email") String email,
                                            @Param("address") String address);
}
