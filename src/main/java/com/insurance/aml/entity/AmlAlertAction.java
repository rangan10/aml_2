package com.insurance.aml.entity;

import com.insurance.aml.enums.AlertAction;
import com.insurance.aml.enums.AlertStage;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Audit trail entry: one row per action taken on an AmlAlert as it
 * moves through the workflow stages.
 */
@Entity
@Table(name = "aml_alert_action")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AmlAlertAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "action_id")
    private Long actionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alert_id", nullable = false)
    private AmlAlert alert;

    @Enumerated(EnumType.STRING)
    @Column(name = "stage", nullable = false, length = 30)
    private AlertStage stage;

    @Column(name = "action_by", nullable = false, length = 100)
    private String actionBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 30)
    private AlertAction action;

    @Column(name = "remarks", length = 1000)
    private String remarks;

    @Column(name = "action_date", nullable = false)
    private LocalDateTime actionDate;

    @PrePersist
    protected void onCreate() {
        if (this.actionDate == null) {
            this.actionDate = LocalDateTime.now();
        }
    }
}
