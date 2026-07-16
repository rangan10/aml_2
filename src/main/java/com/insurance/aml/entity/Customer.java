package com.insurance.aml.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "customer")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "customer_code", nullable = false, unique = true, length = 30)
    private String customerCode;

    @Column(name = "full_name", nullable = false, length = 200)
    private String fullName;

    @Column(name = "pan_number", nullable = false, length = 10)
    private String panNumber;

    @Column(name = "mobile_number", nullable = false, length = 15)
    private String mobileNumber;

    @Column(name = "email", nullable = false, length = 150)
    private String email;

    @Column(name = "address_current", nullable = false, length = 500)
    private String addressCurrent;

    @Column(name = "address_permanent", nullable = false, length = 500)
    private String addressPermanent;

    @Column(name = "annual_income", nullable = false, precision = 18, scale = 2)
    private BigDecimal annualIncome;

    @Enumerated(EnumType.STRING)
    @Column(name = "occupation_type", nullable = false, length = 30)
    private OccupationType occupationType;

    @Column(name = "is_pep", nullable = false)
    private boolean pep;

    @Column(name = "is_nri", nullable = false)
    private boolean nri;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_category", nullable = false, length = 10)
    private RiskCategory riskCategory;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.riskCategory == null) {
            this.riskCategory = RiskCategory.LOW;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
