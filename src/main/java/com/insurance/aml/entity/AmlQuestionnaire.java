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
import java.util.ArrayList;
import java.util.List;

/**
 * A single immutable version of a shared questionnaire template. Any number
 * of tenants may adopt a given version via {@link AmlQuestionnaireTenant};
 * each tenant's own question configuration (mandatory/order/conditional rule)
 * is tracked separately in {@link AmlTenantQuestionnaire}. Structural changes
 * (add/remove/modify question) create a new version once responses already
 * exist against the current one, so {@link AmlQuestionnaireResponse} rows
 * keep pointing at the exact version they were answered against; all tenants
 * assigned to the current version move to the new one together.
 */
@Entity
@Table(name = "aml_questionnaire")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AmlQuestionnaire {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "questionnaire_id")
    private Long questionnaireId;

    @Column(name = "questionnaire_code", nullable = false, length = 50)
    private String questionnaireCode;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", length = 1000)
    private String description;

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
    private AmlQuestionnaire previousVersion;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder.Default
    @OneToMany(mappedBy = "questionnaire", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<AmlTenantQuestionnaire> questions = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "questionnaire", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<AmlQuestionnaireTenant> tenantAssignments = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
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
