package com.insurance.aml.entity;

import com.insurance.aml.enums.QuestionCategory;
import com.insurance.aml.enums.QuestionScope;
import com.insurance.aml.enums.QuestionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A question in the question bank. A question with no tenant is GLOBAL and
 * available to every tenant; a question owned by a tenant is TENANT_SPECIFIC
 * and only usable within that tenant's own questionnaires.
 */
@Entity
@Table(name = "aml_question")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AmlQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "question_id")
    private Long questionId;

    @OneToMany(mappedBy = "question")
    private Set<AmlQuestionTenant> tenantMappings = new HashSet<>();

    @Column(name = "question_code", nullable = false, length = 50)
    private String questionCode;

    @Column(name = "question_text", nullable = false, length = 500)
    private String questionText;

    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false, length = 20)
    private QuestionType questionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "question_scope", nullable = false, length = 20)
    private QuestionScope questionScope;

    @Enumerated(EnumType.STRING)
    @Column(name = "question_category", nullable = false, length = 20)
    private QuestionCategory category;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "version", nullable = false)
    private Float version;

    @Builder.Default
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<AmlQuestionOption> options = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        this.active = true;
//        this.questionScope = this.tenant == null ? QuestionScope.GLOBAL : QuestionScope.TENANT_SPECIFIC;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
