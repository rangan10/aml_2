package com.insurance.aml.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "policy")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Policy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "policy_id")
    private Long policyId;

    @Column(name = "policy_number", nullable = false, unique = true, length = 30)
    private String policyNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "product_type", nullable = false, length = 50)
    private String productType;

    /** Name of the person who proposed/purchased the policy; may differ from the insured. */
    @Column(name = "proposer_name", nullable = false, length = 200)
    private String proposerName;

    @Column(name = "sum_insured", nullable = false, precision = 18, scale = 2)
    private BigDecimal sumInsured;

    @Column(name = "annual_premium", nullable = false, precision = 18, scale = 2)
    private BigDecimal annualPremium;

    @Column(name = "asset_hypothecated", nullable = false)
    private boolean assetHypothecated;

    @Column(name = "policy_start_date", nullable = false)
    private LocalDate policyStartDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "policy_status", nullable = false, length = 20)
    private PolicyStatus policyStatus;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.policyStatus == null) {
            this.policyStatus = PolicyStatus.ACTIVE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
