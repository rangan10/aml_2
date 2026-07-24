package com.insurance.aml.entity;

import com.insurance.aml.enums.QuestionnaireResponseStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A single answered question within a questionnaire response. For
 * SINGLE_CHOICE/MULTI_CHOICE questions, {@code option} is populated (one row
 * per selected option for MULTI_CHOICE); otherwise the raw value is stored
 * in {@code answerText}.`
 */
@Entity
@Table(name = "aml_question_response")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AmlQuestionResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "question_response_id")
    private Long questionResponseId;

    @Column(name = "user_profile_id", nullable = false)
    private Long userProfileId;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private AmlQuestion question;

    @OneToMany(
            mappedBy = "response",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<AmlQuestionOptionResponse> options = new ArrayList<>();

    @Column(name = "answer_text", length = 1000)
    private String answerText;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private QuestionnaireResponseStatus status;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
