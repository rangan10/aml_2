package com.insurance.aml.service;

import com.insurance.aml.entity.Customer;
import com.insurance.aml.entity.OccupationType;
import com.insurance.aml.entity.RiskCategory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * Applies the risk categorization rules:
 * - Government / Salaried            -> LOW risk
 * - Business, Self-Employed, NGO,
 *   Trust, NRI, PEP                  -> HIGH risk
 */
@Service
@Slf4j
public class RiskCategorizationService {

    private static final Set<OccupationType> LOW_RISK_OCCUPATIONS =
            Set.of(OccupationType.GOVERNMENT, OccupationType.SALARIED);

    /**
     * Determines the risk category for a customer based on occupation type,
     * and the PEP / NRI flags (either of which always forces HIGH risk
     * regardless of occupation).
     */
    public RiskCategory categorize(Customer customer) {
        if (customer == null || customer.getOccupationType() == null) {
            throw new IllegalArgumentException("Customer and occupation type must not be null");
        }

        if (customer.isPep() || customer.isNri()) {
            return RiskCategory.HIGH;
        }

        RiskCategory category = LOW_RISK_OCCUPATIONS.contains(customer.getOccupationType())
                ? RiskCategory.LOW
                : RiskCategory.HIGH;

        log.debug("Customer {} with occupation {} categorized as {}",
                customer.getCustomerCode(), customer.getOccupationType(), category);

        return category;
    }

    /**
     * Applies categorize() to the customer and updates its riskCategory field in place.
     */
    public Customer applyRiskCategory(Customer customer) {
        customer.setRiskCategory(categorize(customer));
        return customer;
    }
}
