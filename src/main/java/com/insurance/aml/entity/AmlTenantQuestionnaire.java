package com.insurance.aml.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Maps a question (global or tenant-specific) onto a specific questionnaire
 * version, carrying the tenant's configuration for that question: whether it
 * is mandatory, its display order, and any conditional display rule.
 */
@Entity
@Table(name = "aml_tenant_questionnaire")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AmlTenantQuestionnaire {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tenant_questionnaire_id")
    private Long tenantQuestionnaireId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "questionnaire_id", nullable = false)
    private AmlQuestionnaire questionnaire;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private AmlQuestion question;

    @Column(name = "mandatory", nullable = false)
    private boolean mandatory;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(name = "conditional_rule", length = 1000)
    private String conditionalRule;

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
