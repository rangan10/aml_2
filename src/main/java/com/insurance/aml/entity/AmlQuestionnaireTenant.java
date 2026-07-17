package com.insurance.aml.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Assigns a shared {@link AmlQuestionnaire} definition to a tenant. A
 * questionnaire is a tenant-agnostic template; this join row is what makes it
 * usable by (and visible to) a given tenant. The tenant's own question
 * configuration (mandatory/order/conditional rule) lives separately in
 * {@link AmlTenantQuestionnaire}, one row per question the tenant has added.
 */
@Entity
@Table(name = "aml_questionnaire_tenant")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AmlQuestionnaireTenant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "questionnaire_tenant_id")
    private Long questionnaireTenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "questionnaire_id", nullable = false)
    private AmlQuestionnaire questionnaire;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        this.active = true;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
