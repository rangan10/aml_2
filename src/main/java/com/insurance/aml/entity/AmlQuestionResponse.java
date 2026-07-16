package com.insurance.aml.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * A single answered question within a questionnaire response. For
 * SINGLE_CHOICE/MULTI_CHOICE questions, {@code option} is populated (one row
 * per selected option for MULTI_CHOICE); otherwise the raw value is stored
 * in {@code answerText}.
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "response_id", nullable = false)
    private AmlQuestionnaireResponse response;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private AmlQuestion question;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_id")
    private AmlQuestionOption option;

    @Column(name = "answer_text", length = 1000)
    private String answerText;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
