package com.insurance.aml.entity;

import com.insurance.aml.enums.QuestionnaireStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * A tenant's own versioned instance of a shared {@link AmlQuestionnaire}
 * catalog entry: "tenant T's version N of questionnaire X". It owns that
 * tenant's version lineage, lifecycle (status/effective dates) and question
 * configuration (via {@link AmlTenantQuestionnaire}, one row per question).
 * The tenant's current instance is the {@code ACTIVE} row. A structural change
 * creates a new version of this row (once responses exist against the current
 * one) for that tenant alone, so {@link AmlQuestionnaireResponse} rows keep
 * pointing at the exact tenant-version they were answered against, and other
 * tenants are never affected.
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

    @Column(name = "version", nullable = false)
    private int version;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private QuestionnaireStatus status;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "previous_version_id")
    private AmlQuestionnaireTenant previousVersion;

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
        if (this.status == null) {
            this.status = QuestionnaireStatus.DRAFT;
        }
        if (this.version == 0) {
            this.version = 1;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
