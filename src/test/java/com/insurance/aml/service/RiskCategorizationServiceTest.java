package com.insurance.aml.service;

import com.insurance.aml.enums.OccupationType;
import com.insurance.aml.enums.RiskCategory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RiskCategorizationServiceTest {

    private final RiskCategorizationService service = new RiskCategorizationService();

    @Test
    void governmentOccupation_isLowRisk() {
        Customer customer = baseCustomer(OccupationType.GOVERNMENT);
        assertThat(service.categorize(customer)).isEqualTo(RiskCategory.LOW);
    }

    @Test
    void salariedOccupation_isLowRisk() {
        Customer customer = baseCustomer(OccupationType.SALARIED);
        assertThat(service.categorize(customer)).isEqualTo(RiskCategory.LOW);
    }

    @ParameterizedTest
    @EnumSource(value = OccupationType.class,
            names = {"BUSINESS", "SELF_EMPLOYED", "NGO", "TRUST", "NRI", "PEP"})
    void highRiskOccupations_areCategorizedHigh(OccupationType occupationType) {
        Customer customer = baseCustomer(occupationType);
        assertThat(service.categorize(customer)).isEqualTo(RiskCategory.HIGH);
    }

    @Test
    void pepFlag_forcesHighRisk_evenForSalariedOccupation() {
        Customer customer = baseCustomer(OccupationType.SALARIED);
        customer.setPep(true);
        assertThat(service.categorize(customer)).isEqualTo(RiskCategory.HIGH);
    }

    @Test
    void nriFlag_forcesHighRisk_evenForGovernmentOccupation() {
        Customer customer = baseCustomer(OccupationType.GOVERNMENT);
        customer.setNri(true);
        assertThat(service.categorize(customer)).isEqualTo(RiskCategory.HIGH);
    }

    @Test
    void applyRiskCategory_setsFieldOnCustomer() {
        Customer customer = baseCustomer(OccupationType.BUSINESS);
        service.applyRiskCategory(customer);
        assertThat(customer.getRiskCategory()).isEqualTo(RiskCategory.HIGH);
    }

    @Test
    void nullCustomer_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> service.categorize(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void nullOccupationType_throwsIllegalArgumentException() {
        Customer customer = baseCustomer(null);
        assertThatThrownBy(() -> service.categorize(customer))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private Customer baseCustomer(OccupationType occupationType) {
        return Customer.builder()
                .customerCode("CUST-001")
                .fullName("Test Customer")
                .panNumber("ABCDE1234F")
                .mobileNumber("9876543210")
                .email("test@example.com")
                .addressCurrent("123 Main St")
                .addressPermanent("123 Main St")
                .annualIncome(BigDecimal.valueOf(500000))
                .occupationType(occupationType)
                .pep(false)
                .nri(false)
                .build();
    }
}
